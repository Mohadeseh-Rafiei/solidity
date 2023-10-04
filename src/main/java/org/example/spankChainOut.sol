pragma solidity ^ 0.4.23 ;
 contract LedgerChannel {
 uint256 public numChannels = 0 ;
 struct Channel {
 address [ 2 ] partyAddresses ;
 uint256 [ 4 ] ethBalances ;
 uint256 [ 4 ] erc20Balances ;
 uint256 [ 2 ] initialDeposit ;
 uint256 sequence ;
 uint256 confirmTime ;
 bytes32 VCrootHash ;
 uint256 LCopenTimeout ;
 uint256 updateLCtimeout ;
 bool isOpen ;
 bool isUpdateLCSettling ;
 uint256 numOpenVC ;
 ;
 }
 struct VirtualChannel {
 bool isClose ;
 bool isInSettlementState ;
 uint256 sequence ;
 address challenger ;
 uint256 updateVCtimeout ;
 address partyA ;
 address partyB ;
 address partyI ;
 uint256 [ 2 ] ethBalances ;
 uint256 [ 2 ] erc20Balances ;
 uint256 [ 2 ] bond ;
 ;
 }
 mapping ( bytes32 => VirtualChannel ) public virtualChannels ;
 mapping ( bytes32 => Channel ) public Channels ;
 function createChannel ( bytes32 _lcID , address _partyI , uint256 _confirmTime , address _token , uint256 [ 2 ] _balances ) public payable {
 require ( Channels [ _lcID ] . partyAddresses [ 0 ] == address ( 0 ) , "Channel has already been created." ) ;
 require ( _partyI != 0x0 , "No partyI address provided to LC creation" ) ;
 require ( _balances [ 0 ] >= 0 && _balances [ 1 ] >= 0 , "Balances cannot be negative" ) ;
 Channels [ _lcID ] . partyAddresses [ 0 ] = msg . sender ;
 Channels [ _lcID ] . partyAddresses [ 1 ] = _partyI ;
 if ( _balances [ 0 ] != 0 ) {
 require ( msg . value == _balances [ 0 ] , "Eth balance does not match sent value" ) ;
 Channels [ _lcID ] . ethBalances [ 0 ] = msg . value ;
 }
 if ( _balances [ 1 ] != 0 ) {
 Channels [ _lcID ] . token = ;
 require ( Channels [ _lcID ] . token . transferFrom ( msg . sender , this , _balances [ 1 ] ) , "CreateChannel: token transfer failure" ) ;
 Channels [ _lcID ] . erc20Balances [ 0 ] = _balances [ 1 ] ;
 }
 Channels [ _lcID ] . sequence = 0 ;
 Channels [ _lcID ] . confirmTime = _confirmTime ;
 Channels [ _lcID ] . LCopenTimeout = now + _confirmTime ;
 Channels [ _lcID ] . initialDeposit = _balances ;
 }
 function joinChannel ( bytes32 _lcID , uint256 [ 2 ] _balances ) public payable {
 require ( Channels [ _lcID ] . isOpen == false ) ;
 require ( msg . sender == Channels [ _lcID ] . partyAddresses [ 1 ] ) ;
 if ( _balances [ 0 ] != 0 ) {
 require ( msg . value == _balances [ 0 ] , "state balance does not match sent value" ) ;
 Channels [ _lcID ] . ethBalances [ 1 ] = msg . value ;
 }
 if ( _balances [ 1 ] != 0 ) {
 require ( Channels [ _lcID ] . token . transferFrom ( msg . sender , this , _balances [ 1 ] ) , "joinChannel: token transfer failure" ) ;
 Channels [ _lcID ] . erc20Balances [ 1 ] = _balances [ 1 ] ;
 }
 Channels [ _lcID ] . initialDeposit [ 0 ] += _balances [ 0 ] ;
 Channels [ _lcID ] . initialDeposit [ 1 ] += _balances [ 1 ] ;
 Channels [ _lcID ] . isOpen = true ;
 numChannels ++ ;
 }
 function deposit ( bytes32 _lcID , address recipient , uint256 _balance , bool isToken ) public payable {
 require ( Channels [ _lcID ] . isOpen == true , "Tried adding funds to a closed channel" ) ;
 require ( recipient == Channels [ _lcID ] . partyAddresses [ 0 ] || recipient == Channels [ _lcID ] . partyAddresses [ 1 ] ) ;
 if ( Channels [ _lcID ] . partyAddresses [ 0 ] == recipient ) {
 if ( isToken ) {
 require ( Channels [ _lcID ] . token . transferFrom ( msg . sender , this , _balance ) , "deposit: token transfer failure" ) ;
 Channels [ _lcID ] . erc20Balances [ 2 ] += _balance ;
 }
 else {
 require ( msg . value == _balance , "state balance does not match sent value" ) ;
 Channels [ _lcID ] . ethBalances [ 2 ] += msg . value ;
 }
 }
 if ( Channels [ _lcID ] . partyAddresses [ 1 ] == recipient ) {
 if ( isToken ) {
 require ( Channels [ _lcID ] . token . transferFrom ( msg . sender , this , _balance ) , "deposit: token transfer failure" ) ;
 Channels [ _lcID ] . erc20Balances [ 3 ] += _balance ;
 }
 else {
 require ( msg . value == _balance , "state balance does not match sent value" ) ;
 Channels [ _lcID ] . ethBalances [ 3 ] += msg . value ;
 }
 }
 }
 function updateLCstate ( bytes32 _lcID , uint256 [ 6 ] updateParams , bytes32 _VCroot , string _sigA , string _sigI ) public {
 Channel storage channel = Channels [ _lcID ] ;
 require ( channel . isOpen ) ;
 require ( channel . sequence < updateParams [ 0 ] ) ;
 require ( channel . ethBalances [ 0 ] + channel . ethBalances [ 1 ] >= updateParams [ 2 ] + updateParams [ 3 ] ) ;
 require ( channel . erc20Balances [ 0 ] + channel . erc20Balances [ 1 ] >= updateParams [ 4 ] + updateParams [ 5 ] ) ;
 if ( channel . isUpdateLCSettling == true ) {
 require ( channel . updateLCtimeout > now ) ;
 }
 require ( channel . partyAddresses [ 0 ] == ECTools . recoverSigner ( _state , _sigA ) ) ;
 require ( channel . partyAddresses [ 1 ] == ECTools . recoverSigner ( _state , _sigI ) ) ;
 channel . sequence = updateParams [ 0 ] ;
 channel . numOpenVC = updateParams [ 1 ] ;
 channel . ethBalances [ 0 ] = updateParams [ 2 ] ;
 channel . ethBalances [ 1 ] = updateParams [ 3 ] ;
 channel . erc20Balances [ 0 ] = updateParams [ 4 ] ;
 channel . erc20Balances [ 1 ] = updateParams [ 5 ] ;
 channel . VCrootHash = _VCroot ;
 channel . isUpdateLCSettling = true ;
 channel . updateLCtimeout = now + channel . confirmTime ;
 }
 function initVCstate ( bytes32 _lcID , bytes32 _vcID , bytes _proof , address _partyA , address _partyB , uint256 [ 2 ] _bond , uint256 [ 4 ] _balances , string sigA ) public {
 require ( Channels [ _lcID ] . isOpen , "LC is closed." ) ;
 require ( ! virtualChannels [ _vcID ] . isClose , "VC is closed." ) ;
 require ( Channels [ _lcID ] . updateLCtimeout < now , "LC timeout not over." ) ;
 require ( virtualChannels [ _vcID ] . updateVCtimeout == 0 ) ;
 require ( _partyA == ECTools . recoverSigner ( _initState , sigA ) ) ;
 require ( _isContained ( _initState , _proof , Channels [ _lcID ] . VCrootHash ) == true ) ;
 virtualChannels [ _vcID ] . partyA = _partyA ;
 virtualChannels [ _vcID ] . partyB = _partyB ;
 virtualChannels [ _vcID ] . sequence = uint256 ( 0 ) ;
 virtualChannels [ _vcID ] . ethBalances [ 0 ] = _balances [ 0 ] ;
 virtualChannels [ _vcID ] . ethBalances [ 1 ] = _balances [ 1 ] ;
 virtualChannels [ _vcID ] . erc20Balances [ 0 ] = _balances [ 2 ] ;
 virtualChannels [ _vcID ] . erc20Balances [ 1 ] = _balances [ 3 ] ;
 virtualChannels [ _vcID ] . bond = _bond ;
 virtualChannels [ _vcID ] . updateVCtimeout = now + Channels [ _lcID ] . confirmTime ;
 virtualChannels [ _vcID ] . isInSettlementState = true ;
 }
 function settleVC ( bytes32 _lcID , bytes32 _vcID , uint256 updateSeq , address _partyA , address _partyB , uint256 [ 4 ] updateBal , string sigA ) public {
 require ( Channels [ _lcID ] . isOpen , "LC is closed." ) ;
 require ( ! virtualChannels [ _vcID ] . isClose , "VC is closed." ) ;
 require ( virtualChannels [ _vcID ] . sequence < updateSeq , "VC sequence is higher than update sequence." ) ;
 require ( virtualChannels [ _vcID ] . ethBalances [ 1 ] < updateBal [ 1 ] && virtualChannels [ _vcID ] . erc20Balances [ 1 ] < updateBal [ 3 ] , "State updates may only increase recipient balance." ) ;
 require ( virtualChannels [ _vcID ] . bond [ 0 ] == updateBal [ 0 ] + updateBal [ 1 ] && virtualChannels [ _vcID ] . bond [ 1 ] == updateBal [ 2 ] + updateBal [ 3 ] , "Incorrect balances for bonded amount" ) ;
 require ( Channels [ _lcID ] . updateLCtimeout < now ) ;
 require ( virtualChannels [ _vcID ] . partyA == ECTools . recoverSigner ( _updateState , sigA ) ) ;
 virtualChannels [ _vcID ] . challenger = msg . sender ;
 virtualChannels [ _vcID ] . sequence = updateSeq ;
 virtualChannels [ _vcID ] . ethBalances [ 0 ] = updateBal [ 0 ] ;
 virtualChannels [ _vcID ] . ethBalances [ 1 ] = updateBal [ 1 ] ;
 virtualChannels [ _vcID ] . erc20Balances [ 0 ] = updateBal [ 2 ] ;
 virtualChannels [ _vcID ] . erc20Balances [ 1 ] = updateBal [ 3 ] ;
 virtualChannels [ _vcID ] . updateVCtimeout = now + Channels [ _lcID ] . confirmTime ;
 }
 function closeVirtualChannel ( bytes32 _lcID , bytes32 _vcID ) public {
 require ( Channels [ _lcID ] . isOpen , "LC is closed." ) ;
 require ( virtualChannels [ _vcID ] . isInSettlementState , "VC is not in settlement state." ) ;
 require ( virtualChannels [ _vcID ] . updateVCtimeout < now , "Update vc timeout has not elapsed." ) ;
 require ( ! virtualChannels [ _vcID ] . isClose , "VC is already closed" ) ;
 Channels [ _lcID ] . numOpenVC -- ;
 virtualChannels [ _vcID ] . isClose = true ;
 if ( virtualChannels [ _vcID ] . partyA == Channels [ _lcID ] . partyAddresses [ 0 ] ) {
 Channels [ _lcID ] . ethBalances [ 0 ] += virtualChannels [ _vcID ] . ethBalances [ 0 ] ;
 Channels [ _lcID ] . ethBalances [ 1 ] += virtualChannels [ _vcID ] . ethBalances [ 1 ] ;
 Channels [ _lcID ] . erc20Balances [ 0 ] += virtualChannels [ _vcID ] . erc20Balances [ 0 ] ;
 Channels [ _lcID ] . erc20Balances [ 1 ] += virtualChannels [ _vcID ] . erc20Balances [ 1 ] ;
 }
 else if ( virtualChannels [ _vcID ] . partyB == Channels [ _lcID ] . partyAddresses [ 0 ] ) {
 Channels [ _lcID ] . ethBalances [ 0 ] += virtualChannels [ _vcID ] . ethBalances [ 1 ] ;
 Channels [ _lcID ] . ethBalances [ 1 ] += virtualChannels [ _vcID ] . ethBalances [ 0 ] ;
 Channels [ _lcID ] . erc20Balances [ 0 ] += virtualChannels [ _vcID ] . erc20Balances [ 1 ] ;
 Channels [ _lcID ] . erc20Balances [ 1 ] += virtualChannels [ _vcID ] . erc20Balances [ 0 ] ;
 }
 }
 }
 <EOF>