package org.example;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.util.List;

public class SolidityPreprocessor {
    public static String preprocessSolidity(String solidityCode) {
        SolidityLexer lexer = new SolidityLexer(CharStreams.fromString(solidityCode));
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        SolidityParser parser = new SolidityParser(tokens);
        ParseTree tree = parser.sourceUnit(); // Obtain the root of the parse tree after parsing

        // Step 1: Make AST
        SolidityAST ast = new SolidityAST(tree);

        // Step 2: Handle modifiers and continuation sections
        SolidityModifierListener modifierListener = new SolidityModifierListener(ast);
        ParseTreeWalker.DEFAULT.walk(modifierListener, tree);
        ast = modifierListener.getModifiedTree();// Update modifiedTree

        // Step 3: Remove events and emits
        SolidityEventEmitRemover eventEmitRemover = new SolidityEventEmitRemover(ast);
        ParseTreeWalker.DEFAULT.walk(eventEmitRemover, tree);
        ast = eventEmitRemover.getModifiedTree(); // Update modifiedTree

        // Step 4: Remove pure, view, and constant functions
        SolidityFunctionRemover functionRemover = new SolidityFunctionRemover(ast);
        ParseTreeWalker.DEFAULT.walk(functionRemover, tree);
        ast = functionRemover.getModifiedTree(); // Update modifiedTree

        // Step 5: Remove interfaces
        SolidityInterfaceRemover interfaceRemover = new SolidityInterfaceRemover(ast);
        ParseTreeWalker.DEFAULT.walk(interfaceRemover, tree);
        ast = interfaceRemover.getModifiedTree(); // Update modifiedTree

        SolidityAssemblyRemover assemblyRemover = new SolidityAssemblyRemover(ast);
        ParseTreeWalker.DEFAULT.walk(assemblyRemover, tree);
        ast = assemblyRemover.getModifiedTree();

        // Step 6: Keep functions with important features
        cleanCode codeCleaner = new cleanCode(ast);
        ast = codeCleaner.getModifiedTree(); // Update modifiedTree

        return ast.getText();
    }

    private static String tokensToString(List<CommonToken> tokens) {
        StringBuilder builder = new StringBuilder();
        for (CommonToken token : tokens) {
            builder.append(token.getText());
        }
        return builder.toString();
    }


    public static void main(String[] args) {
        // Sample Solidity code
        String solidityCode = "/**\n" +
                " *Submitted for verification at Etherscan.io on 2018-10-06\n" +
                "*/\n" +
                "\n" +
                "pragma solidity ^0.4.23;\n" +
                "// produced by the Solididy File Flattener (c) David Appleton 2018\n" +
                "// contact : dave@akomba.com\n" +
                "// released under Apache 2.0 licence\n" +
                "contract Token {\n" +
                "    /* This is a slight change to the ERC20 base standard.\n" +
                "    function totalSupply() constant returns (uint256 supply);\n" +
                "    is replaced with:\n" +
                "    uint256 public totalSupply;\n" +
                "    This automatically creates a getter function for the totalSupply.\n" +
                "    This is moved to the base contract since public getter functions are not\n" +
                "    currently recognised as an implementation of the matching abstract\n" +
                "    function by the compiler.\n" +
                "    */\n" +
                "    /// total amount of tokens\n" +
                "    uint256 public totalSupply;\n" +
                "\n" +
                "    /// @param _owner The address from which the balance will be retrieved\n" +
                "    /// @return The balance\n" +
                "    function balanceOf(address _owner) public constant returns (uint256 balance);\n" +
                "\n" +
                "    /// @notice send `_value` token to `_to` from `msg.sender`\n" +
                "    /// @param _to The address of the recipient\n" +
                "    /// @param _value The amount of token to be transferred\n" +
                "    /// @return Whether the transfer was successful or not\n" +
                "    function transfer(address _to, uint256 _value) public returns (bool success);\n" +
                "\n" +
                "    /// @notice send `_value` token to `_to` from `_from` on the condition it is approved by `_from`\n" +
                "    /// @param _from The address of the sender\n" +
                "    /// @param _to The address of the recipient\n" +
                "    /// @param _value The amount of token to be transferred\n" +
                "    /// @return Whether the transfer was successful or not\n" +
                "    function transferFrom(address _from, address _to, uint256 _value) public returns (bool success);\n" +
                "\n" +
                "    /// @notice `msg.sender` approves `_spender` to spend `_value` tokens\n" +
                "    /// @param _spender The address of the account able to transfer the tokens\n" +
                "    /// @param _value The amount of tokens to be approved for transfer\n" +
                "    /// @return Whether the approval was successful or not\n" +
                "    function approve(address _spender, uint256 _value) public returns (bool success);\n" +
                "\n" +
                "    /// @param _owner The address of the account owning tokens\n" +
                "    /// @param _spender The address of the account able to transfer the tokens\n" +
                "    /// @return Amount of remaining tokens allowed to spent\n" +
                "    function allowance(address _owner, address _spender) public constant returns (uint256 remaining);\n" +
                "\n" +
                "    event Transfer(address indexed _from, address indexed _to, uint256 _value);\n" +
                "    event Approval(address indexed _owner, address indexed _spender, uint256 _value);\n" +
                "}\n" +
                "\n" +
                "library ECTools {\n" +
                "\n" +
                "    // @dev Recovers the address which has signed a message\n" +
                "    // @thanks https://gist.github.com/axic/5b33912c6f61ae6fd96d6c4a47afde6d\n" +
                "    function recoverSigner(bytes32 _hashedMsg, string _sig) public pure returns (address) {\n" +
                "        require(_hashedMsg != 0x00);\n" +
                "\n" +
                "        // need this for test RPC\n" +
                "        bytes memory prefix = \"\\x19Ethereum Signed Message:\\n32\";\n" +
                "        bytes32 prefixedHash = keccak256(abi.encodePacked(prefix, _hashedMsg));\n" +
                "\n" +
                "        if (bytes(_sig).length != 132) {\n" +
                "            return 0x0;\n" +
                "        }\n" +
                "        bytes32 r;\n" +
                "        bytes32 s;\n" +
                "        uint8 v;\n" +
                "        bytes memory sig = hexstrToBytes(substring(_sig, 2, 132));\n" +
                "        assembly {\n" +
                "            r := mload(add(sig, 32))\n" +
                "            s := mload(add(sig, 64))\n" +
                "            v := byte(0, mload(add(sig, 96)))\n" +
                "        }\n" +
                "        if (v < 27) {\n" +
                "            v += 27;\n" +
                "        }\n" +
                "        if (v < 27 || v > 28) {\n" +
                "            return 0x0;\n" +
                "        }\n" +
                "        return ecrecover(prefixedHash, v, r, s);\n" +
                "    }\n" +
                "\n" +
                "    // @dev Verifies if the message is signed by an address\n" +
                "    function isSignedBy(bytes32 _hashedMsg, string _sig, address _addr) public pure returns (bool) {\n" +
                "        require(_addr != 0x0);\n" +
                "\n" +
                "        return _addr == recoverSigner(_hashedMsg, _sig);\n" +
                "    }\n" +
                "\n" +
                "    // @dev Converts an hexstring to bytes\n" +
                "    function hexstrToBytes(string _hexstr) public pure returns (bytes) {\n" +
                "        uint len = bytes(_hexstr).length;\n" +
                "        require(len % 2 == 0);\n" +
                "\n" +
                "        bytes memory bstr = bytes(new string(len / 2));\n" +
                "        uint k = 0;\n" +
                "        string memory s;\n" +
                "        string memory r;\n" +
                "        for (uint i = 0; i < len; i += 2) {\n" +
                "            s = substring(_hexstr, i, i + 1);\n" +
                "            r = substring(_hexstr, i + 1, i + 2);\n" +
                "            uint p = parseInt16Char(s) * 16 + parseInt16Char(r);\n" +
                "            bstr[k++] = uintToBytes32(p)[31];\n" +
                "        }\n" +
                "        return bstr;\n" +
                "    }\n" +
                "\n" +
                "    // @dev Parses a hexchar, like 'a', and returns its hex value, in this case 10\n" +
                "    function parseInt16Char(string _char) public pure returns (uint) {\n" +
                "        bytes memory bresult = bytes(_char);\n" +
                "        // bool decimals = false;\n" +
                "        if ((bresult[0] >= 48) && (bresult[0] <= 57)) {\n" +
                "            return uint(bresult[0]) - 48;\n" +
                "        } else if ((bresult[0] >= 65) && (bresult[0] <= 70)) {\n" +
                "            return uint(bresult[0]) - 55;\n" +
                "        } else if ((bresult[0] >= 97) && (bresult[0] <= 102)) {\n" +
                "            return uint(bresult[0]) - 87;\n" +
                "        } else {\n" +
                "            revert();\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    // @dev Converts a uint to a bytes32\n" +
                "    // @thanks https://ethereum.stackexchange.com/questions/4170/how-to-convert-a-uint-to-bytes-in-solidity\n" +
                "    function uintToBytes32(uint _uint) public pure returns (bytes b) {\n" +
                "        b = new bytes(32);\n" +
                "        assembly {mstore(add(b, 32), _uint)}\n" +
                "    }\n" +
                "\n" +
                "    // @dev Hashes the signed message\n" +
                "    // @ref https://github.com/ethereum/go-ethereum/issues/3731#issuecomment-293866868\n" +
                "    function toEthereumSignedMessage(string _msg) public pure returns (bytes32) {\n" +
                "        uint len = bytes(_msg).length;\n" +
                "        require(len > 0);\n" +
                "        bytes memory prefix = \"\\x19Ethereum Signed Message:\\n\";\n" +
                "        return keccak256(abi.encodePacked(prefix, uintToString(len), _msg));\n" +
                "    }\n" +
                "\n" +
                "    // @dev Converts a uint in a string\n" +
                "    function uintToString(uint _uint) public pure returns (string str) {\n" +
                "        uint len = 0;\n" +
                "        uint m = _uint + 0;\n" +
                "        while (m != 0) {\n" +
                "            len++;\n" +
                "            m /= 10;\n" +
                "        }\n" +
                "        bytes memory b = new bytes(len);\n" +
                "        uint i = len - 1;\n" +
                "        while (_uint != 0) {\n" +
                "            uint remainder = _uint % 10;\n" +
                "            _uint = _uint / 10;\n" +
                "            b[i--] = byte(48 + remainder);\n" +
                "        }\n" +
                "        str = string(b);\n" +
                "    }\n" +
                "\n" +
                "\n" +
                "    // @dev extract a substring\n" +
                "    // @thanks https://ethereum.stackexchange.com/questions/31457/substring-in-solidity\n" +
                "    function substring(string _str, uint _startIndex, uint _endIndex) public pure returns (string) {\n" +
                "        bytes memory strBytes = bytes(_str);\n" +
                "        require(_startIndex <= _endIndex);\n" +
                "        require(_startIndex >= 0);\n" +
                "        require(_endIndex <= strBytes.length);\n" +
                "\n" +
                "        bytes memory result = new bytes(_endIndex - _startIndex);\n" +
                "        for (uint i = _startIndex; i < _endIndex; i++) {\n" +
                "            result[i - _startIndex] = strBytes[i];\n" +
                "        }\n" +
                "        return string(result);\n" +
                "    }\n" +
                "}\n" +
                "contract StandardToken is Token {\n" +
                "\n" +
                "    function transfer(address _to, uint256 _value) public returns (bool success) {\n" +
                "        //Default assumes totalSupply can't be over max (2^256 - 1).\n" +
                "        //If your token leaves out totalSupply and can issue more tokens as time goes on, you need to check if it doesn't wrap.\n" +
                "        //Replace the if with this one instead.\n" +
                "        //require(balances[msg.sender] >= _value && balances[_to] + _value > balances[_to]);\n" +
                "        require(balances[msg.sender] >= _value);\n" +
                "        balances[msg.sender] -= _value;\n" +
                "        balances[_to] += _value;\n" +
                "        emit Transfer(msg.sender, _to, _value);\n" +
                "        return true;\n" +
                "    }\n" +
                "\n" +
                "    function transferFrom(address _from, address _to, uint256 _value) public returns (bool success) {\n" +
                "        //same as above. Replace this line with the following if you want to protect against wrapping uints.\n" +
                "        //require(balances[_from] >= _value && allowed[_from][msg.sender] >= _value && balances[_to] + _value > balances[_to]);\n" +
                "        require(balances[_from] >= _value && allowed[_from][msg.sender] >= _value);\n" +
                "        balances[_to] += _value;\n" +
                "        balances[_from] -= _value;\n" +
                "        allowed[_from][msg.sender] -= _value;\n" +
                "        emit Transfer(_from, _to, _value);\n" +
                "        return true;\n" +
                "    }\n" +
                "\n" +
                "    function balanceOf(address _owner) public constant returns (uint256 balance) {\n" +
                "        return balances[_owner];\n" +
                "    }\n" +
                "\n" +
                "    function approve(address _spender, uint256 _value) public returns (bool success) {\n" +
                "        allowed[msg.sender][_spender] = _value;\n" +
                "        emit Approval(msg.sender, _spender, _value);\n" +
                "        return true;\n" +
                "    }\n" +
                "\n" +
                "    function allowance(address _owner, address _spender) public constant returns (uint256 remaining) {\n" +
                "      return allowed[_owner][_spender];\n" +
                "    }\n" +
                "\n" +
                "    mapping (address => uint256) balances;\n" +
                "    mapping (address => mapping (address => uint256)) allowed;\n" +
                "}\n" +
                "\n" +
                "contract HumanStandardToken is StandardToken {\n" +
                "\n" +
                "    /* Public variables of the token */\n" +
                "\n" +
                "    /*\n" +
                "    NOTE:\n" +
                "    The following variables are OPTIONAL vanities. One does not have to include them.\n" +
                "    They allow one to customise the token contract & in no way influences the core functionality.\n" +
                "    Some wallets/interfaces might not even bother to look at this information.\n" +
                "    */\n" +
                "    string public name;                   //fancy name: eg Simon Bucks\n" +
                "    uint8 public decimals;                //How many decimals to show. ie. There could 1000 base units with 3 decimals. Meaning 0.980 SBX = 980 base units. It's like comparing 1 wei to 1 ether.\n" +
                "    string public symbol;                 //An identifier: eg SBX\n" +
                "    string public version = 'H0.1';       //human 0.1 standard. Just an arbitrary versioning scheme.\n" +
                "\n" +
                "    constructor(\n" +
                "        uint256 _initialAmount,\n" +
                "        string _tokenName,\n" +
                "        uint8 _decimalUnits,\n" +
                "        string _tokenSymbol\n" +
                "        ) public {\n" +
                "        balances[msg.sender] = _initialAmount;               // Give the creator all initial tokens\n" +
                "        totalSupply = _initialAmount;                        // Update total supply\n" +
                "        name = _tokenName;                                   // Set the name for display purposes\n" +
                "        decimals = _decimalUnits;                            // Amount of decimals for display purposes\n" +
                "        symbol = _tokenSymbol;                               // Set the symbol for display purposes\n" +
                "    }\n" +
                "\n" +
                "    /* Approves and then calls the receiving contract */\n" +
                "    function approveAndCall(address _spender, uint256 _value, bytes _extraData) public returns (bool success) {\n" +
                "        allowed[msg.sender][_spender] = _value;\n" +
                "        emit Approval(msg.sender, _spender, _value);\n" +
                "\n" +
                "        //call the receiveApproval function on the contract you want to be notified. This crafts the function signature manually so one doesn't have to include a contract in here just for this.\n" +
                "        //receiveApproval(address _from, uint256 _value, address _tokenContract, bytes _extraData)\n" +
                "        //it is assumed that when does this that the call *should* succeed, otherwise one would use vanilla approve instead.\n" +
                "        require(_spender.call(bytes4(bytes32(keccak256(\"receiveApproval(address,uint256,address,bytes)\"))), msg.sender, _value, this, _extraData));\n" +
                "        return true;\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "contract LedgerChannel {\n" +
                "\n" +
                "    string public constant NAME = \"Ledger Channel\";\n" +
                "    string public constant VERSION = \"0.0.1\";\n" +
                "\n" +
                "    uint256 public numChannels = 0;\n" +
                "\n" +
                "    event DidLCOpen (\n" +
                "        bytes32 indexed channelId,\n" +
                "        address indexed partyA,\n" +
                "        address indexed partyI,\n" +
                "        uint256 ethBalanceA,\n" +
                "        address token,\n" +
                "        uint256 tokenBalanceA,\n" +
                "        uint256 LCopenTimeout\n" +
                "    );\n" +
                "\n" +
                "    event DidLCJoin (\n" +
                "        bytes32 indexed channelId,\n" +
                "        uint256 ethBalanceI,\n" +
                "        uint256 tokenBalanceI\n" +
                "    );\n" +
                "\n" +
                "    event DidLCDeposit (\n" +
                "        bytes32 indexed channelId,\n" +
                "        address indexed recipient,\n" +
                "        uint256 deposit,\n" +
                "        bool isToken\n" +
                "    );\n" +
                "\n" +
                "    event DidLCUpdateState (\n" +
                "        bytes32 indexed channelId, \n" +
                "        uint256 sequence, \n" +
                "        uint256 numOpenVc, \n" +
                "        uint256 ethBalanceA,\n" +
                "        uint256 tokenBalanceA,\n" +
                "        uint256 ethBalanceI,\n" +
                "        uint256 tokenBalanceI,\n" +
                "        bytes32 vcRoot,\n" +
                "        uint256 updateLCtimeout\n" +
                "    );\n" +
                "\n" +
                "    event DidLCClose (\n" +
                "        bytes32 indexed channelId,\n" +
                "        uint256 sequence,\n" +
                "        uint256 ethBalanceA,\n" +
                "        uint256 tokenBalanceA,\n" +
                "        uint256 ethBalanceI,\n" +
                "        uint256 tokenBalanceI\n" +
                "    );\n" +
                "\n" +
                "    event DidVCInit (\n" +
                "        bytes32 indexed lcId, \n" +
                "        bytes32 indexed vcId, \n" +
                "        bytes proof, \n" +
                "        uint256 sequence, \n" +
                "        address partyA, \n" +
                "        address partyB, \n" +
                "        uint256 balanceA, \n" +
                "        uint256 balanceB \n" +
                "    );\n" +
                "\n" +
                "    event DidVCSettle (\n" +
                "        bytes32 indexed lcId, \n" +
                "        bytes32 indexed vcId,\n" +
                "        uint256 updateSeq, \n" +
                "        uint256 updateBalA, \n" +
                "        uint256 updateBalB,\n" +
                "        address challenger,\n" +
                "        uint256 updateVCtimeout\n" +
                "    );\n" +
                "\n" +
                "    event DidVCClose(\n" +
                "        bytes32 indexed lcId, \n" +
                "        bytes32 indexed vcId, \n" +
                "        uint256 balanceA, \n" +
                "        uint256 balanceB\n" +
                "    );\n" +
                "\n" +
                "    struct Channel {\n" +
                "        //TODO: figure out if it's better just to split arrays by balances/deposits instead of eth/erc20\n" +
                "        address[2] partyAddresses; // 0: partyA 1: partyI\n" +
                "        uint256[4] ethBalances; // 0: balanceA 1:balanceI 2:depositedA 3:depositedI\n" +
                "        uint256[4] erc20Balances; // 0: balanceA 1:balanceI 2:depositedA 3:depositedI\n" +
                "        uint256[2] initialDeposit; // 0: eth 1: tokens\n" +
                "        uint256 sequence;\n" +
                "        uint256 confirmTime;\n" +
                "        bytes32 VCrootHash;\n" +
                "        uint256 LCopenTimeout;\n" +
                "        uint256 updateLCtimeout; // when update LC times out\n" +
                "        bool isOpen; // true when both parties have joined\n" +
                "        bool isUpdateLCSettling;\n" +
                "        uint256 numOpenVC;\n" +
                "        HumanStandardToken token;\n" +
                "    }\n" +
                "\n" +
                "    // virtual-channel state\n" +
                "    struct VirtualChannel {\n" +
                "        bool isClose;\n" +
                "        bool isInSettlementState;\n" +
                "        uint256 sequence;\n" +
                "        address challenger; // Initiator of challenge\n" +
                "        uint256 updateVCtimeout; // when update VC times out\n" +
                "        // channel state\n" +
                "        address partyA; // VC participant A\n" +
                "        address partyB; // VC participant B\n" +
                "        address partyI; // LC hub\n" +
                "        uint256[2] ethBalances;\n" +
                "        uint256[2] erc20Balances;\n" +
                "        uint256[2] bond;\n" +
                "        HumanStandardToken token;\n" +
                "    }\n" +
                "\n" +
                "    mapping(bytes32 => VirtualChannel) public virtualChannels;\n" +
                "    mapping(bytes32 => Channel) public Channels;\n" +
                "\n" +
                "    function createChannel(\n" +
                "        bytes32 _lcID,\n" +
                "        address _partyI,\n" +
                "        uint256 _confirmTime,\n" +
                "        address _token,\n" +
                "        uint256[2] _balances // [eth, token]\n" +
                "    ) \n" +
                "        public\n" +
                "        payable \n" +
                "    {\n" +
                "        require(Channels[_lcID].partyAddresses[0] == address(0), \"Channel has already been created.\");\n" +
                "        require(_partyI != 0x0, \"No partyI address provided to LC creation\");\n" +
                "        require(_balances[0] >= 0 && _balances[1] >= 0, \"Balances cannot be negative\");\n" +
                "        // Set initial ledger channel state\n" +
                "        // Alice must execute this and we assume the initial state \n" +
                "        // to be signed from this requirement\n" +
                "        // Alternative is to check a sig as in joinChannel\n" +
                "        Channels[_lcID].partyAddresses[0] = msg.sender;\n" +
                "        Channels[_lcID].partyAddresses[1] = _partyI;\n" +
                "\n" +
                "        if(_balances[0] != 0) {\n" +
                "            require(msg.value == _balances[0], \"Eth balance does not match sent value\");\n" +
                "            Channels[_lcID].ethBalances[0] = msg.value;\n" +
                "        } \n" +
                "        if(_balances[1] != 0) {\n" +
                "            Channels[_lcID].token = HumanStandardToken(_token);\n" +
                "            require(Channels[_lcID].token.transferFrom(msg.sender, this, _balances[1]),\"CreateChannel: token transfer failure\");\n" +
                "            Channels[_lcID].erc20Balances[0] = _balances[1];\n" +
                "        }\n" +
                "\n" +
                "        Channels[_lcID].sequence = 0;\n" +
                "        Channels[_lcID].confirmTime = _confirmTime;\n" +
                "        // is close flag, lc state sequence, number open vc, vc root hash, partyA... \n" +
                "        //Channels[_lcID].stateHash = keccak256(uint256(0), uint256(0), uint256(0), bytes32(0x0), bytes32(msg.sender), bytes32(_partyI), balanceA, balanceI);\n" +
                "        Channels[_lcID].LCopenTimeout = now + _confirmTime;\n" +
                "        Channels[_lcID].initialDeposit = _balances;\n" +
                "\n" +
                "        emit DidLCOpen(_lcID, msg.sender, _partyI, _balances[0], _token, _balances[1], Channels[_lcID].LCopenTimeout);\n" +
                "    }\n" +
                "\n" +
                "    function LCOpenTimeout(bytes32 _lcID) public {\n" +
                "        require(msg.sender == Channels[_lcID].partyAddresses[0] && Channels[_lcID].isOpen == false);\n" +
                "        require(now > Channels[_lcID].LCopenTimeout);\n" +
                "\n" +
                "        if(Channels[_lcID].initialDeposit[0] != 0) {\n" +
                "            Channels[_lcID].partyAddresses[0].transfer(Channels[_lcID].ethBalances[0]);\n" +
                "        } \n" +
                "        if(Channels[_lcID].initialDeposit[1] != 0) {\n" +
                "            require(Channels[_lcID].token.transfer(Channels[_lcID].partyAddresses[0], Channels[_lcID].erc20Balances[0]),\"CreateChannel: token transfer failure\");\n" +
                "        }\n" +
                "\n" +
                "        emit DidLCClose(_lcID, 0, Channels[_lcID].ethBalances[0], Channels[_lcID].erc20Balances[0], 0, 0);\n" +
                "\n" +
                "        // only safe to delete since no action was taken on this channel\n" +
                "        delete Channels[_lcID];\n" +
                "    }\n" +
                "\n" +
                "    function joinChannel(bytes32 _lcID, uint256[2] _balances) public payable {\n" +
                "        // require the channel is not open yet\n" +
                "        require(Channels[_lcID].isOpen == false);\n" +
                "        require(msg.sender == Channels[_lcID].partyAddresses[1]);\n" +
                "\n" +
                "        if(_balances[0] != 0) {\n" +
                "            require(msg.value == _balances[0], \"state balance does not match sent value\");\n" +
                "            Channels[_lcID].ethBalances[1] = msg.value;\n" +
                "        } \n" +
                "        if(_balances[1] != 0) {\n" +
                "            require(Channels[_lcID].token.transferFrom(msg.sender, this, _balances[1]),\"joinChannel: token transfer failure\");\n" +
                "            Channels[_lcID].erc20Balances[1] = _balances[1];          \n" +
                "        }\n" +
                "\n" +
                "        Channels[_lcID].initialDeposit[0]+=_balances[0];\n" +
                "        Channels[_lcID].initialDeposit[1]+=_balances[1];\n" +
                "        // no longer allow joining functions to be called\n" +
                "        Channels[_lcID].isOpen = true;\n" +
                "        numChannels++;\n" +
                "\n" +
                "        emit DidLCJoin(_lcID, _balances[0], _balances[1]);\n" +
                "    }\n" +
                "\n" +
                "\n" +
                "    // additive updates of monetary state\n" +
                "    // TODO check this for attack vectors\n" +
                "    function deposit(bytes32 _lcID, address recipient, uint256 _balance, bool isToken) public payable {\n" +
                "        require(Channels[_lcID].isOpen == true, \"Tried adding funds to a closed channel\");\n" +
                "        require(recipient == Channels[_lcID].partyAddresses[0] || recipient == Channels[_lcID].partyAddresses[1]);\n" +
                "\n" +
                "        //if(Channels[_lcID].token)\n" +
                "\n" +
                "        if (Channels[_lcID].partyAddresses[0] == recipient) {\n" +
                "            if(isToken) {\n" +
                "                require(Channels[_lcID].token.transferFrom(msg.sender, this, _balance),\"deposit: token transfer failure\");\n" +
                "                Channels[_lcID].erc20Balances[2] += _balance;\n" +
                "            } else {\n" +
                "                require(msg.value == _balance, \"state balance does not match sent value\");\n" +
                "                Channels[_lcID].ethBalances[2] += msg.value;\n" +
                "            }\n" +
                "        }\n" +
                "\n" +
                "        if (Channels[_lcID].partyAddresses[1] == recipient) {\n" +
                "            if(isToken) {\n" +
                "                require(Channels[_lcID].token.transferFrom(msg.sender, this, _balance),\"deposit: token transfer failure\");\n" +
                "                Channels[_lcID].erc20Balances[3] += _balance;\n" +
                "            } else {\n" +
                "                require(msg.value == _balance, \"state balance does not match sent value\");\n" +
                "                Channels[_lcID].ethBalances[3] += msg.value; \n" +
                "            }\n" +
                "        }\n" +
                "        \n" +
                "        emit DidLCDeposit(_lcID, recipient, _balance, isToken);\n" +
                "    }\n" +
                "\n" +
                "    // TODO: Check there are no open virtual channels, the client should have cought this before signing a close LC state update\n" +
                "    function consensusCloseChannel(\n" +
                "        bytes32 _lcID, \n" +
                "        uint256 _sequence, \n" +
                "        uint256[4] _balances, // 0: ethBalanceA 1:ethBalanceI 2:tokenBalanceA 3:tokenBalanceI\n" +
                "        string _sigA, \n" +
                "        string _sigI\n" +
                "    ) \n" +
                "        public \n" +
                "    {\n" +
                "        // assume num open vc is 0 and root hash is 0x0\n" +
                "        //require(Channels[_lcID].sequence < _sequence);\n" +
                "        require(Channels[_lcID].isOpen == true);\n" +
                "        uint256 totalEthDeposit = Channels[_lcID].initialDeposit[0] + Channels[_lcID].ethBalances[2] + Channels[_lcID].ethBalances[3];\n" +
                "        uint256 totalTokenDeposit = Channels[_lcID].initialDeposit[1] + Channels[_lcID].erc20Balances[2] + Channels[_lcID].erc20Balances[3];\n" +
                "        require(totalEthDeposit == _balances[0] + _balances[1]);\n" +
                "        require(totalTokenDeposit == _balances[2] + _balances[3]);\n" +
                "\n" +
                "        bytes32 _state = keccak256(\n" +
                "            abi.encodePacked(\n" +
                "                _lcID,\n" +
                "                true,\n" +
                "                _sequence,\n" +
                "                uint256(0),\n" +
                "                bytes32(0x0),\n" +
                "                Channels[_lcID].partyAddresses[0], \n" +
                "                Channels[_lcID].partyAddresses[1], \n" +
                "                _balances[0], \n" +
                "                _balances[1],\n" +
                "                _balances[2],\n" +
                "                _balances[3]\n" +
                "            )\n" +
                "        );\n" +
                "\n" +
                "        require(Channels[_lcID].partyAddresses[0] == ECTools.recoverSigner(_state, _sigA));\n" +
                "        require(Channels[_lcID].partyAddresses[1] == ECTools.recoverSigner(_state, _sigI));\n" +
                "\n" +
                "        Channels[_lcID].isOpen = false;\n" +
                "\n" +
                "        if(_balances[0] != 0 || _balances[1] != 0) {\n" +
                "            Channels[_lcID].partyAddresses[0].transfer(_balances[0]);\n" +
                "            Channels[_lcID].partyAddresses[1].transfer(_balances[1]);\n" +
                "        }\n" +
                "\n" +
                "        if(_balances[2] != 0 || _balances[3] != 0) {\n" +
                "            require(Channels[_lcID].token.transfer(Channels[_lcID].partyAddresses[0], _balances[2]),\"happyCloseChannel: token transfer failure\");\n" +
                "            require(Channels[_lcID].token.transfer(Channels[_lcID].partyAddresses[1], _balances[3]),\"happyCloseChannel: token transfer failure\");          \n" +
                "        }\n" +
                "\n" +
                "        numChannels--;\n" +
                "\n" +
                "        emit DidLCClose(_lcID, _sequence, _balances[0], _balances[1], _balances[2], _balances[3]);\n" +
                "    }\n" +
                "\n" +
                "    // Byzantine functions\n" +
                "\n" +
                "    function updateLCstate(\n" +
                "        bytes32 _lcID, \n" +
                "        uint256[6] updateParams, // [sequence, numOpenVc, ethbalanceA, ethbalanceI, tokenbalanceA, tokenbalanceI]\n" +
                "        bytes32 _VCroot, \n" +
                "        string _sigA, \n" +
                "        string _sigI\n" +
                "    ) \n" +
                "        public \n" +
                "    {\n" +
                "        Channel storage channel = Channels[_lcID];\n" +
                "        require(channel.isOpen);\n" +
                "        require(channel.sequence < updateParams[0]); // do same as vc sequence check\n" +
                "        require(channel.ethBalances[0] + channel.ethBalances[1] >= updateParams[2] + updateParams[3]);\n" +
                "        require(channel.erc20Balances[0] + channel.erc20Balances[1] >= updateParams[4] + updateParams[5]);\n" +
                "\n" +
                "        if(channel.isUpdateLCSettling == true) { \n" +
                "            require(channel.updateLCtimeout > now);\n" +
                "        }\n" +
                "      \n" +
                "        bytes32 _state = keccak256(\n" +
                "            abi.encodePacked(\n" +
                "                _lcID,\n" +
                "                false, \n" +
                "                updateParams[0], \n" +
                "                updateParams[1], \n" +
                "                _VCroot, \n" +
                "                channel.partyAddresses[0], \n" +
                "                channel.partyAddresses[1], \n" +
                "                updateParams[2], \n" +
                "                updateParams[3],\n" +
                "                updateParams[4], \n" +
                "                updateParams[5]\n" +
                "            )\n" +
                "        );\n" +
                "\n" +
                "        require(channel.partyAddresses[0] == ECTools.recoverSigner(_state, _sigA));\n" +
                "        require(channel.partyAddresses[1] == ECTools.recoverSigner(_state, _sigI));\n" +
                "\n" +
                "        // update LC state\n" +
                "        channel.sequence = updateParams[0];\n" +
                "        channel.numOpenVC = updateParams[1];\n" +
                "        channel.ethBalances[0] = updateParams[2];\n" +
                "        channel.ethBalances[1] = updateParams[3];\n" +
                "        channel.erc20Balances[0] = updateParams[4];\n" +
                "        channel.erc20Balances[1] = updateParams[5];\n" +
                "        channel.VCrootHash = _VCroot;\n" +
                "        channel.isUpdateLCSettling = true;\n" +
                "        channel.updateLCtimeout = now + channel.confirmTime;\n" +
                "\n" +
                "        // make settlement flag\n" +
                "\n" +
                "        emit DidLCUpdateState (\n" +
                "            _lcID, \n" +
                "            updateParams[0], \n" +
                "            updateParams[1], \n" +
                "            updateParams[2], \n" +
                "            updateParams[3],\n" +
                "            updateParams[4],\n" +
                "            updateParams[5], \n" +
                "            _VCroot,\n" +
                "            channel.updateLCtimeout\n" +
                "        );\n" +
                "    }\n" +
                "\n" +
                "    // supply initial state of VC to \"prime\" the force push game  \n" +
                "    function initVCstate(\n" +
                "        bytes32 _lcID, \n" +
                "        bytes32 _vcID, \n" +
                "        bytes _proof, \n" +
                "        address _partyA, \n" +
                "        address _partyB, \n" +
                "        uint256[2] _bond,\n" +
                "        uint256[4] _balances, // 0: ethBalanceA 1:ethBalanceI 2:tokenBalanceA 3:tokenBalanceI\n" +
                "        string sigA\n" +
                "    ) \n" +
                "        public \n" +
                "    {\n" +
                "        require(Channels[_lcID].isOpen, \"LC is closed.\");\n" +
                "        // sub-channel must be open\n" +
                "        require(!virtualChannels[_vcID].isClose, \"VC is closed.\");\n" +
                "        // Check time has passed on updateLCtimeout and has not passed the time to store a vc state\n" +
                "        require(Channels[_lcID].updateLCtimeout < now, \"LC timeout not over.\");\n" +
                "        // prevent rentry of initializing vc state\n" +
                "        require(virtualChannels[_vcID].updateVCtimeout == 0);\n" +
                "        // partyB is now Ingrid\n" +
                "        bytes32 _initState = keccak256(\n" +
                "            abi.encodePacked(_vcID, uint256(0), _partyA, _partyB, _bond[0], _bond[1], _balances[0], _balances[1], _balances[2], _balances[3])\n" +
                "        );\n" +
                "\n" +
                "        // Make sure Alice has signed initial vc state (A/B in oldState)\n" +
                "        require(_partyA == ECTools.recoverSigner(_initState, sigA));\n" +
                "\n" +
                "        // Check the oldState is in the root hash\n" +
                "        require(_isContained(_initState, _proof, Channels[_lcID].VCrootHash) == true);\n" +
                "\n" +
                "        virtualChannels[_vcID].partyA = _partyA; // VC participant A\n" +
                "        virtualChannels[_vcID].partyB = _partyB; // VC participant B\n" +
                "        virtualChannels[_vcID].sequence = uint256(0);\n" +
                "        virtualChannels[_vcID].ethBalances[0] = _balances[0];\n" +
                "        virtualChannels[_vcID].ethBalances[1] = _balances[1];\n" +
                "        virtualChannels[_vcID].erc20Balances[0] = _balances[2];\n" +
                "        virtualChannels[_vcID].erc20Balances[1] = _balances[3];\n" +
                "        virtualChannels[_vcID].bond = _bond;\n" +
                "        virtualChannels[_vcID].updateVCtimeout = now + Channels[_lcID].confirmTime;\n" +
                "        virtualChannels[_vcID].isInSettlementState = true;\n" +
                "\n" +
                "        emit DidVCInit(_lcID, _vcID, _proof, uint256(0), _partyA, _partyB, _balances[0], _balances[1]);\n" +
                "    }\n" +
                "\n" +
                "    //TODO: verify state transition since the hub did not agree to this state\n" +
                "    // make sure the A/B balances are not beyond ingrids bonds  \n" +
                "    // Params: vc init state, vc final balance, vcID\n" +
                "    function settleVC(\n" +
                "        bytes32 _lcID, \n" +
                "        bytes32 _vcID, \n" +
                "        uint256 updateSeq, \n" +
                "        address _partyA, \n" +
                "        address _partyB,\n" +
                "        uint256[4] updateBal, // [ethupdateBalA, ethupdateBalB, tokenupdateBalA, tokenupdateBalB]\n" +
                "        string sigA\n" +
                "    ) \n" +
                "        public \n" +
                "    {\n" +
                "        require(Channels[_lcID].isOpen, \"LC is closed.\");\n" +
                "        // sub-channel must be open\n" +
                "        require(!virtualChannels[_vcID].isClose, \"VC is closed.\");\n" +
                "        require(virtualChannels[_vcID].sequence < updateSeq, \"VC sequence is higher than update sequence.\");\n" +
                "        require(\n" +
                "            virtualChannels[_vcID].ethBalances[1] < updateBal[1] && virtualChannels[_vcID].erc20Balances[1] < updateBal[3],\n" +
                "            \"State updates may only increase recipient balance.\"\n" +
                "        );\n" +
                "        require(\n" +
                "            virtualChannels[_vcID].bond[0] == updateBal[0] + updateBal[1] &&\n" +
                "            virtualChannels[_vcID].bond[1] == updateBal[2] + updateBal[3], \n" +
                "            \"Incorrect balances for bonded amount\");\n" +
                "        // Check time has passed on updateLCtimeout and has not passed the time to store a vc state\n" +
                "        // virtualChannels[_vcID].updateVCtimeout should be 0 on uninitialized vc state, and this should\n" +
                "        // fail if initVC() isn't called first\n" +
                "        // require(Channels[_lcID].updateLCtimeout < now && now < virtualChannels[_vcID].updateVCtimeout);\n" +
                "        require(Channels[_lcID].updateLCtimeout < now); // for testing!\n" +
                "\n" +
                "        bytes32 _updateState = keccak256(\n" +
                "            abi.encodePacked(\n" +
                "                _vcID, \n" +
                "                updateSeq, \n" +
                "                _partyA, \n" +
                "                _partyB, \n" +
                "                virtualChannels[_vcID].bond[0], \n" +
                "                virtualChannels[_vcID].bond[1], \n" +
                "                updateBal[0], \n" +
                "                updateBal[1], \n" +
                "                updateBal[2], \n" +
                "                updateBal[3]\n" +
                "            )\n" +
                "        );\n" +
                "\n" +
                "        // Make sure Alice has signed a higher sequence new state\n" +
                "        require(virtualChannels[_vcID].partyA == ECTools.recoverSigner(_updateState, sigA));\n" +
                "\n" +
                "        // store VC data\n" +
                "        // we may want to record who is initiating on-chain settles\n" +
                "        virtualChannels[_vcID].challenger = msg.sender;\n" +
                "        virtualChannels[_vcID].sequence = updateSeq;\n" +
                "\n" +
                "        // channel state\n" +
                "        virtualChannels[_vcID].ethBalances[0] = updateBal[0];\n" +
                "        virtualChannels[_vcID].ethBalances[1] = updateBal[1];\n" +
                "        virtualChannels[_vcID].erc20Balances[0] = updateBal[2];\n" +
                "        virtualChannels[_vcID].erc20Balances[1] = updateBal[3];\n" +
                "\n" +
                "        virtualChannels[_vcID].updateVCtimeout = now + Channels[_lcID].confirmTime;\n" +
                "\n" +
                "        emit DidVCSettle(_lcID, _vcID, updateSeq, updateBal[0], updateBal[1], msg.sender, virtualChannels[_vcID].updateVCtimeout);\n" +
                "    }\n" +
                "\n" +
                "    function closeVirtualChannel(bytes32 _lcID, bytes32 _vcID) public {\n" +
                "        // require(updateLCtimeout > now)\n" +
                "        require(Channels[_lcID].isOpen, \"LC is closed.\");\n" +
                "        require(virtualChannels[_vcID].isInSettlementState, \"VC is not in settlement state.\");\n" +
                "        require(virtualChannels[_vcID].updateVCtimeout < now, \"Update vc timeout has not elapsed.\");\n" +
                "        require(!virtualChannels[_vcID].isClose, \"VC is already closed\");\n" +
                "        // reduce the number of open virtual channels stored on LC\n" +
                "        Channels[_lcID].numOpenVC--;\n" +
                "        // close vc flags\n" +
                "        virtualChannels[_vcID].isClose = true;\n" +
                "        // re-introduce the balances back into the LC state from the settled VC\n" +
                "        // decide if this lc is alice or bob in the vc\n" +
                "        if(virtualChannels[_vcID].partyA == Channels[_lcID].partyAddresses[0]) {\n" +
                "            Channels[_lcID].ethBalances[0] += virtualChannels[_vcID].ethBalances[0];\n" +
                "            Channels[_lcID].ethBalances[1] += virtualChannels[_vcID].ethBalances[1];\n" +
                "\n" +
                "            Channels[_lcID].erc20Balances[0] += virtualChannels[_vcID].erc20Balances[0];\n" +
                "            Channels[_lcID].erc20Balances[1] += virtualChannels[_vcID].erc20Balances[1];\n" +
                "        } else if (virtualChannels[_vcID].partyB == Channels[_lcID].partyAddresses[0]) {\n" +
                "            Channels[_lcID].ethBalances[0] += virtualChannels[_vcID].ethBalances[1];\n" +
                "            Channels[_lcID].ethBalances[1] += virtualChannels[_vcID].ethBalances[0];\n" +
                "\n" +
                "            Channels[_lcID].erc20Balances[0] += virtualChannels[_vcID].erc20Balances[1];\n" +
                "            Channels[_lcID].erc20Balances[1] += virtualChannels[_vcID].erc20Balances[0];\n" +
                "        }\n" +
                "\n" +
                "        emit DidVCClose(_lcID, _vcID, virtualChannels[_vcID].erc20Balances[0], virtualChannels[_vcID].erc20Balances[1]);\n" +
                "    }\n" +
                "\n" +
                "\n" +
                "    // todo: allow ethier lc.end-user to nullify the settled LC state and return to off-chain\n" +
                "    function byzantineCloseChannel(bytes32 _lcID) public {\n" +
                "        Channel storage channel = Channels[_lcID];\n" +
                "\n" +
                "        // check settlement flag\n" +
                "        require(channel.isOpen, \"Channel is not open\");\n" +
                "        require(channel.isUpdateLCSettling == true);\n" +
                "        require(channel.numOpenVC == 0);\n" +
                "        require(channel.updateLCtimeout < now, \"LC timeout over.\");\n" +
                "\n" +
                "        // if off chain state update didnt reblance deposits, just return to deposit owner\n" +
                "        uint256 totalEthDeposit = channel.initialDeposit[0] + channel.ethBalances[2] + channel.ethBalances[3];\n" +
                "        uint256 totalTokenDeposit = channel.initialDeposit[1] + channel.erc20Balances[2] + channel.erc20Balances[3];\n" +
                "\n" +
                "        uint256 possibleTotalEthBeforeDeposit = channel.ethBalances[0] + channel.ethBalances[1]; \n" +
                "        uint256 possibleTotalTokenBeforeDeposit = channel.erc20Balances[0] + channel.erc20Balances[1];\n" +
                "\n" +
                "        if(possibleTotalEthBeforeDeposit < totalEthDeposit) {\n" +
                "            channel.ethBalances[0]+=channel.ethBalances[2];\n" +
                "            channel.ethBalances[1]+=channel.ethBalances[3];\n" +
                "        } else {\n" +
                "            require(possibleTotalEthBeforeDeposit == totalEthDeposit);\n" +
                "        }\n" +
                "\n" +
                "        if(possibleTotalTokenBeforeDeposit < totalTokenDeposit) {\n" +
                "            channel.erc20Balances[0]+=channel.erc20Balances[2];\n" +
                "            channel.erc20Balances[1]+=channel.erc20Balances[3];\n" +
                "        } else {\n" +
                "            require(possibleTotalTokenBeforeDeposit == totalTokenDeposit);\n" +
                "        }\n" +
                "\n" +
                "        // reentrancy\n" +
                "        uint256 ethbalanceA = channel.ethBalances[0];\n" +
                "        uint256 ethbalanceI = channel.ethBalances[1];\n" +
                "        uint256 tokenbalanceA = channel.erc20Balances[0];\n" +
                "        uint256 tokenbalanceI = channel.erc20Balances[1];\n" +
                "\n" +
                "        channel.ethBalances[0] = 0;\n" +
                "        channel.ethBalances[1] = 0;\n" +
                "        channel.erc20Balances[0] = 0;\n" +
                "        channel.erc20Balances[1] = 0;\n" +
                "\n" +
                "        if(ethbalanceA != 0 || ethbalanceI != 0) {\n" +
                "            channel.partyAddresses[0].transfer(ethbalanceA);\n" +
                "            channel.partyAddresses[1].transfer(ethbalanceI);\n" +
                "        }\n" +
                "\n" +
                "        if(tokenbalanceA != 0 || tokenbalanceI != 0) {\n" +
                "            require(\n" +
                "                channel.token.transfer(channel.partyAddresses[0], tokenbalanceA),\n" +
                "                \"byzantineCloseChannel: token transfer failure\"\n" +
                "            );\n" +
                "            require(\n" +
                "                channel.token.transfer(channel.partyAddresses[1], tokenbalanceI),\n" +
                "                \"byzantineCloseChannel: token transfer failure\"\n" +
                "            );          \n" +
                "        }\n" +
                "\n" +
                "        channel.isOpen = false;\n" +
                "        numChannels--;\n" +
                "\n" +
                "        emit DidLCClose(_lcID, channel.sequence, ethbalanceA, ethbalanceI, tokenbalanceA, tokenbalanceI);\n" +
                "    }\n" +
                "\n" +
                "    function _isContained(bytes32 _hash, bytes _proof, bytes32 _root) internal pure returns (bool) {\n" +
                "        bytes32 cursor = _hash;\n" +
                "        bytes32 proofElem;\n" +
                "\n" +
                "        for (uint256 i = 64; i <= _proof.length; i += 32) {\n" +
                "            assembly { proofElem := mload(add(_proof, i)) }\n" +
                "\n" +
                "            if (cursor < proofElem) {\n" +
                "                cursor = keccak256(abi.encodePacked(cursor, proofElem));\n" +
                "            } else {\n" +
                "                cursor = keccak256(abi.encodePacked(proofElem, cursor));\n" +
                "            }\n" +
                "        }\n" +
                "\n" +
                "        return cursor == _root;\n" +
                "    }\n" +
                "\n" +
                "    //Struct Getters\n" +
                "    function getChannel(bytes32 id) public view returns (\n" +
                "        address[2],\n" +
                "        uint256[4],\n" +
                "        uint256[4],\n" +
                "        uint256[2],\n" +
                "        uint256,\n" +
                "        uint256,\n" +
                "        bytes32,\n" +
                "        uint256,\n" +
                "        uint256,\n" +
                "        bool,\n" +
                "        bool,\n" +
                "        uint256\n" +
                "    ) {\n" +
                "        Channel memory channel = Channels[id];\n" +
                "        return (\n" +
                "            channel.partyAddresses,\n" +
                "            channel.ethBalances,\n" +
                "            channel.erc20Balances,\n" +
                "            channel.initialDeposit,\n" +
                "            channel.sequence,\n" +
                "            channel.confirmTime,\n" +
                "            channel.VCrootHash,\n" +
                "            channel.LCopenTimeout,\n" +
                "            channel.updateLCtimeout,\n" +
                "            channel.isOpen,\n" +
                "            channel.isUpdateLCSettling,\n" +
                "            channel.numOpenVC\n" +
                "        );\n" +
                "    }\n" +
                "\n" +
                "    function getVirtualChannel(bytes32 id) public view returns(\n" +
                "        bool,\n" +
                "        bool,\n" +
                "        uint256,\n" +
                "        address,\n" +
                "        uint256,\n" +
                "        address,\n" +
                "        address,\n" +
                "        address,\n" +
                "        uint256[2],\n" +
                "        uint256[2],\n" +
                "        uint256[2]\n" +
                "    ) {\n" +
                "        VirtualChannel memory virtualChannel = virtualChannels[id];\n" +
                "        return(\n" +
                "            virtualChannel.isClose,\n" +
                "            virtualChannel.isInSettlementState,\n" +
                "            virtualChannel.sequence,\n" +
                "            virtualChannel.challenger,\n" +
                "            virtualChannel.updateVCtimeout,\n" +
                "            virtualChannel.partyA,\n" +
                "            virtualChannel.partyB,\n" +
                "            virtualChannel.partyI,\n" +
                "            virtualChannel.ethBalances,\n" +
                "            virtualChannel.erc20Balances,\n" +
                "            virtualChannel.bond\n" +
                "        );\n" +
                "    }\n" +
                "}";

        // Preprocess the Solidity code
        String modifiedCode = preprocessSolidity(solidityCode);

        // Output the modified Solidity code
        System.out.println(modifiedCode);
    }
}
