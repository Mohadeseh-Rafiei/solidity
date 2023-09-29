// SPDX-License-Identifier: MIT
pragma solidity ^0.4.23;

contract LedgerChannel {

   uint256 public numChannels = 0;

    struct Channel {
        //TODO: figure out if it's better just to split arrays by balances/deposits instead of eth/erc20
        address[2] partyAddresses; // 0: partyA 1: partyI
        uint256[4] ethBalances; // 0: balanceA 1:balanceI 2:depositedA 3:depositedI
        uint256[4] erc20Balances; // 0: balanceA 1:balanceI 2:depositedA 3:depositedI
        uint256[2] initialDeposit; // 0: eth 1: tokens
        uint256 sequence;
        uint256 confirmTime;
        bytes32 VCrootHash;
        uint256 LCopenTimeout;
        uint256 updateLCtimeout; // when update LC times out
        bool isOpen; // true when both parties have joined
        bool isUpdateLCSettling;
        uint256 numOpenVC;
    }

    // virtual-channel state
    struct VirtualChannel {
        bool isClose;
        bool isInSettlementState;
        uint256 sequence;
        address challenger; // Initiator of challenge
        uint256 updateVCtimeout; // when update VC times out
        // channel state
        address partyA; // VC participant A
        address partyB; // VC participant B
        address partyI; // LC hub
        uint256[2] ethBalances;
        uint256[2] erc20Balances;
        uint256[2] bond;
    }

    mapping(bytes32 => VirtualChannel) public virtualChannels;
    mapping(bytes32 => Channel) public Channels;

    function createChannel(bytes32 _lcID,address _partyI,uint256 _confirmTime,address _token,uint256[2] _balances) 
        public payable {
        require(Channels[_lcID].partyAddresses[0] == address(0), "Channel has already been created.");
        require(_partyI != 0x0, "No partyI address provided to LC creation");
        require(_balances[0] >= 0 && _balances[1] >= 0, "Balances cannot be negative");
       
        Channels[_lcID].partyAddresses[0] = msg.sender;
        Channels[_lcID].partyAddresses[1] = _partyI;

        if(_balances[0] != 0) {
            require(msg.value == _balances[0], "Eth balance does not match sent value");
            Channels[_lcID].ethBalances[0] = msg.value;
        } 
        if(_balances[1] != 0) {
            Channels[_lcID].token = HumanStandardToken(_token);
            require(Channels[_lcID].token.transferFrom(msg.sender, this, _balances[1]),"CreateChannel: token transfer failure");
            Channels[_lcID].erc20Balances[0] = _balances[1];
        }

        Channels[_lcID].sequence = 0;
        Channels[_lcID].confirmTime = _confirmTime;
        Channels[_lcID].LCopenTimeout = now + _confirmTime;
        Channels[_lcID].initialDeposit = _balances;
    }

    function LCOpenTimeout(bytes32 _lcID) public {
        require(msg.sender == Channels[_lcID].partyAddresses[0] && Channels[_lcID].isOpen == false);
        require(now > Channels[_lcID].LCopenTimeout);

        if(Channels[_lcID].initialDeposit[0] != 0) {
            Channels[_lcID].partyAddresses[0].transfer(Channels[_lcID].ethBalances[0]);
        } 
        if(Channels[_lcID].initialDeposit[1] != 0) {
            require(Channels[_lcID].token.transfer(Channels[_lcID].partyAddresses[0], Channels[_lcID].erc20Balances[0]),"CreateChannel: token transfer failure");
        }

        // only safe to delete since no action was taken on this channel
        delete Channels[_lcID];
    }

    function joinChannel(bytes32 _lcID, uint256[2] _balances) public payable {
        // require the channel is not open yet
        require(Channels[_lcID].isOpen == false);
        require(msg.sender == Channels[_lcID].partyAddresses[1]);

        if(_balances[0] != 0) {
            require(msg.value == _balances[0], "state balance does not match sent value");
            Channels[_lcID].ethBalances[1] = msg.value;
        } 
        if(_balances[1] != 0) {
            require(Channels[_lcID].token.transferFrom(msg.sender, this, _balances[1]),"joinChannel: token transfer failure");
            Channels[_lcID].erc20Balances[1] = _balances[1];          
        }

        Channels[_lcID].initialDeposit[0]+=_balances[0];
        Channels[_lcID].initialDeposit[1]+=_balances[1];
        Channels[_lcID].isOpen = true;
        numChannels++;
    }

    function deposit(bytes32 _lcID, address recipient, uint256 _balance, bool isToken) public payable {
        require(Channels[_lcID].isOpen == true, "Tried adding funds to a closed channel");
        require(recipient == Channels[_lcID].partyAddresses[0] || recipient == Channels[_lcID].partyAddresses[1]);

        if (Channels[_lcID].partyAddresses[0] == recipient) {
            if(isToken) {
                require(Channels[_lcID].token.transferFrom(msg.sender, this, _balance),"deposit: token transfer failure");
                Channels[_lcID].erc20Balances[2] += _balance;
            } else {
                require(msg.value == _balance, "state balance does not match sent value");
                Channels[_lcID].ethBalances[2] += msg.value;
            }
        }

        if (Channels[_lcID].partyAddresses[1] == recipient) {
            if(isToken) {
                require(Channels[_lcID].token.transferFrom(msg.sender, this, _balance),"deposit: token transfer failure");
                Channels[_lcID].erc20Balances[3] += _balance;
            } else {
                require(msg.value == _balance, "state balance does not match sent value");
                Channels[_lcID].ethBalances[3] += msg.value; 
            }
        }
    }

    // TODO: Check there are no open virtual channels, the client should have cought this before signing a close LC state update
    function consensusCloseChannel(bytes32 _lcID,uint256 _sequence, 
        uint256[4] _balances, // 0: ethBalanceA 1:ethBalanceI 2:tokenBalanceA 3:tokenBalanceI
        string _sigA,string _sigI) public {
        require(Channels[_lcID].isOpen == true);
        uint256 totalEthDeposit = Channels[_lcID].initialDeposit[0] + Channels[_lcID].ethBalances[2] + Channels[_lcID].ethBalances[3];
        uint256 totalTokenDeposit = Channels[_lcID].initialDeposit[1] + Channels[_lcID].erc20Balances[2] + Channels[_lcID].erc20Balances[3];
        require(totalEthDeposit == _balances[0] + _balances[1]);
        require(totalTokenDeposit == _balances[2] + _balances[3]);

        //require(Channels[_lcID].partyAddresses[0] == ECTools.recoverSigner(_state, _sigA));
        //require(Channels[_lcID].partyAddresses[1] == ECTools.recoverSigner(_state, _sigI));

        Channels[_lcID].isOpen = false;

        if(_balances[0] != 0 || _balances[1] != 0) {
            Channels[_lcID].partyAddresses[0].transfer(_balances[0]);
            Channels[_lcID].partyAddresses[1].transfer(_balances[1]);
        }

        if(_balances[2] != 0 || _balances[3] != 0) {
            require(Channels[_lcID].token.transfer(Channels[_lcID].partyAddresses[0], _balances[2]),"happyCloseChannel: token transfer failure");
            require(Channels[_lcID].token.transfer(Channels[_lcID].partyAddresses[1], _balances[3]),"happyCloseChannel: token transfer failure");          
        }

        numChannels--;
    }

    // Byzantine functions

    function updateLCstate(bytes32 _lcID, 
        uint256[6] updateParams, // [sequence, numOpenVc, ethbalanceA, ethbalanceI, tokenbalanceA, tokenbalanceI]
        bytes32 _VCroot,string _sigA,string _sigI) public {
        Channel storage channel = Channels[_lcID];
        require(channel.isOpen);
        require(channel.sequence < updateParams[0]); // do same as vc sequence check
        require(channel.ethBalances[0] + channel.ethBalances[1] >= updateParams[2] + updateParams[3]);
        require(channel.erc20Balances[0] + channel.erc20Balances[1] >= updateParams[4] + updateParams[5]);

        if(channel.isUpdateLCSettling == true) { 
            require(channel.updateLCtimeout > now);
        }

        //require(channel.partyAddresses[0] == ECTools.recoverSigner(_state, _sigA));
        //require(channel.partyAddresses[1] == ECTools.recoverSigner(_state, _sigI));

        // update LC state
        channel.sequence = updateParams[0];
        channel.numOpenVC = updateParams[1];
        channel.ethBalances[0] = updateParams[2];
        channel.ethBalances[1] = updateParams[3];
        channel.erc20Balances[0] = updateParams[4];
        channel.erc20Balances[1] = updateParams[5];
        channel.VCrootHash = _VCroot;
        channel.isUpdateLCSettling = true;
        channel.updateLCtimeout = now + channel.confirmTime;
    }

    function closeVirtualChannel(bytes32 _lcID, bytes32 _vcID) public {
        require(Channels[_lcID].isOpen, "LC is closed.");
        require(virtualChannels[_vcID].isInSettlementState, "VC is not in settlement state.");
        require(virtualChannels[_vcID].updateVCtimeout < now, "Update vc timeout has not elapsed.");
        require(!virtualChannels[_vcID].isClose, "VC is already closed");
        // reduce the number of open virtual channels stored on LC
        Channels[_lcID].numOpenVC--;
        // close vc flags
        virtualChannels[_vcID].isClose = true;
        // re-introduce the balances back into the LC state from the settled VC
        // decide if this lc is alice or bob in the vc
        if(virtualChannels[_vcID].partyA == Channels[_lcID].partyAddresses[0]) {
            Channels[_lcID].ethBalances[0] += virtualChannels[_vcID].ethBalances[0];
            Channels[_lcID].ethBalances[1] += virtualChannels[_vcID].ethBalances[1];

            Channels[_lcID].erc20Balances[0] += virtualChannels[_vcID].erc20Balances[0];
            Channels[_lcID].erc20Balances[1] += virtualChannels[_vcID].erc20Balances[1];
        } else if (virtualChannels[_vcID].partyB == Channels[_lcID].partyAddresses[0]) {
            Channels[_lcID].ethBalances[0] += virtualChannels[_vcID].ethBalances[1];
            Channels[_lcID].ethBalances[1] += virtualChannels[_vcID].ethBalances[0];

            Channels[_lcID].erc20Balances[0] += virtualChannels[_vcID].erc20Balances[1];
            Channels[_lcID].erc20Balances[1] += virtualChannels[_vcID].erc20Balances[0];
        }
    }


    // todo: allow ethier lc.end-user to nullify the settled LC state and return to off-chain
    function byzantineCloseChannel(bytes32 _lcID) public {
        Channel storage channel = Channels[_lcID];

        // check settlement flag
        require(channel.isOpen, "Channel is not open");
        require(channel.isUpdateLCSettling == true);
        require(channel.numOpenVC == 0);
        require(channel.updateLCtimeout < now, "LC timeout over.");

        // if off chain state update didnt reblance deposits, just return to deposit owner
        uint256 totalEthDeposit = channel.initialDeposit[0] + channel.ethBalances[2] + channel.ethBalances[3];
        uint256 totalTokenDeposit = channel.initialDeposit[1] + channel.erc20Balances[2] + channel.erc20Balances[3];

        uint256 possibleTotalEthBeforeDeposit = channel.ethBalances[0] + channel.ethBalances[1]; 
        uint256 possibleTotalTokenBeforeDeposit = channel.erc20Balances[0] + channel.erc20Balances[1];

        if(possibleTotalEthBeforeDeposit < totalEthDeposit) {
            channel.ethBalances[0]+=channel.ethBalances[2];
            channel.ethBalances[1]+=channel.ethBalances[3];
        } else {
            require(possibleTotalEthBeforeDeposit == totalEthDeposit);
        }

        if(possibleTotalTokenBeforeDeposit < totalTokenDeposit) {
            channel.erc20Balances[0]+=channel.erc20Balances[2];
            channel.erc20Balances[1]+=channel.erc20Balances[3];
        } else {
            require(possibleTotalTokenBeforeDeposit == totalTokenDeposit);
        }

        // reentrancy
        uint256 ethbalanceA = channel.ethBalances[0];
        uint256 ethbalanceI = channel.ethBalances[1];
        uint256 tokenbalanceA = channel.erc20Balances[0];
        uint256 tokenbalanceI = channel.erc20Balances[1];

        channel.ethBalances[0] = 0;
        channel.ethBalances[1] = 0;
        channel.erc20Balances[0] = 0;
        channel.erc20Balances[1] = 0;

        if(ethbalanceA != 0 || ethbalanceI != 0) {
            channel.partyAddresses[0].transfer(ethbalanceA);
            channel.partyAddresses[1].transfer(ethbalanceI);
        }

        if(tokenbalanceA != 0 || tokenbalanceI != 0) {
            require(
                channel.token.transfer(channel.partyAddresses[0], tokenbalanceA),
                "byzantineCloseChannel: token transfer failure"
            );
            require(
                channel.token.transfer(channel.partyAddresses[1], tokenbalanceI),
                "byzantineCloseChannel: token transfer failure"
            );          
        }

        channel.isOpen = false;
        numChannels--;
    }
}