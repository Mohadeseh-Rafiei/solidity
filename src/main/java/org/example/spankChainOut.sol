contract LedgerChannel {
 Int public numChannels = 0 ;
 struct Channel {
 address [ 2 ] partyAddresses ;
 Int [ 4 ] ethBalances ;
 Int [ 4 ] erc20Balances ;
 Int [ 2 ] initialDeposit ;
 Int sequence ;
 Int confirmTime ;
 bytes32 VCrootHash ;
 Int LCopenTimeout ;
 Int updateLCtimeout ;
 bool isOpen ;
 bool isUpdateLCSettling ;
 Int numOpenVC ;
 }
 struct VirtualChannel {
 bool isClose ;
 bool isInSettlementState ;
 Int sequence ;
 address challenger ;
 Int updateVCtimeout ;
 address partyA ;
 address partyB ;
 address partyI ;
 Int [ 2 ] ethBalances ;
 Int [ 2 ] erc20Balances ;
 Int [ 2 ] bond ;
 ;
 }
 mapping ( bytes32 => VirtualChannel ) public virtualChannels ;
 mapping ( bytes32 => Channel ) public Channels ;
 }
 function LCOpenTimeout ( bytes32 _lcID ) public {
 require ( msg . sender == Channels [ _lcID ] . partyAddresses [ 0 ] && Channels [ _lcID ] . isOpen == false ) ;
 require ( now > Channels [ _lcID ] . LCopenTimeout ) ;
 if ( Channels [ _lcID ] . initialDeposit [ 0 ] != 0 ) {
 Channels [ _lcID ] . partyAddresses [ 0 ] . transfer ( Channels [ _lcID ] . ethBalances [ 0 ] ) ;
 }
 if ( Channels [ _lcID ] . initialDeposit [ 1 ] != 0 ) {
 require ( Channels [ _lcID ] . token . transfer ( Channels [ _lcID ] . partyAddresses [ 0 ] , Channels [ _lcID ] . erc20Balances [ 0 ] ) , "CreateChannel: token transfer failure" ) ;
 }
 delete Channels [ _lcID ] ;
 }
 function consensusCloseChannel ( bytes32 _lcID , Int _sequence , Int [ 4 ] _balances , string _sigA , string _sigI ) public {
 require ( Channels [ _lcID ] . isOpen == true ) ;
 Int totalEthDeposit = Channels [ _lcID ] . initialDeposit [ 0 ] + Channels [ _lcID ] . ethBalances [ 2 ] + Channels [ _lcID ] . ethBalances [ 3 ] ;
 Int totalTokenDeposit = Channels [ _lcID ] . initialDeposit [ 1 ] + Channels [ _lcID ] . erc20Balances [ 2 ] + Channels [ _lcID ] . erc20Balances [ 3 ] ;
 require ( totalEthDeposit == _balances [ 0 ] + _balances [ 1 ] ) ;
 require ( totalTokenDeposit == _balances [ 2 ] + _balances [ 3 ] ) ;
 require ( Channels [ _lcID ] . partyAddresses [ 0 ] == ECTools . recoverSigner ( _state , _sigA ) ) ;
 require ( Channels [ _lcID ] . partyAddresses [ 1 ] == ECTools . recoverSigner ( _state , _sigI ) ) ;
 Channels [ _lcID ] . isOpen = false ;
 if ( _balances [ 0 ] != 0 || _balances [ 1 ] != 0 ) {
 Channels [ _lcID ] . partyAddresses [ 0 ] . transfer ( _balances [ 0 ] ) ;
 Channels [ _lcID ] . partyAddresses [ 1 ] . transfer ( _balances [ 1 ] ) ;
 }
 if ( _balances [ 2 ] != 0 || _balances [ 3 ] != 0 ) {
 require ( Channels [ _lcID ] . token . transfer ( Channels [ _lcID ] . partyAddresses [ 0 ] , _balances [ 2 ] ) , "happyCloseChannel: token transfer failure" ) ;
 require ( Channels [ _lcID ] . token . transfer ( Channels [ _lcID ] . partyAddresses [ 1 ] , _balances [ 3 ] ) , "happyCloseChannel: token transfer failure" ) ;
 }
 numChannels -- ;
 }
 function byzantineCloseChannel ( bytes32 _lcID ) public {
 Channel storage channel = Channels [ _lcID ] ;
 require ( channel . isOpen , "Channel is not open" ) ;
 require ( channel . isUpdateLCSettling == true ) ;
 require ( channel . numOpenVC == 0 ) ;
 require ( channel . updateLCtimeout < now , "LC timeout over." ) ;
 Int totalEthDeposit = channel . initialDeposit [ 0 ] + channel . ethBalances [ 2 ] + channel . ethBalances [ 3 ] ;
 Int totalTokenDeposit = channel . initialDeposit [ 1 ] + channel . erc20Balances [ 2 ] + channel . erc20Balances [ 3 ] ;
 Int possibleTotalEthBeforeDeposit = channel . ethBalances [ 0 ] + channel . ethBalances [ 1 ] ;
 Int possibleTotalTokenBeforeDeposit = channel . erc20Balances [ 0 ] + channel . erc20Balances [ 1 ] ;
 if ( possibleTotalEthBeforeDeposit < totalEthDeposit ) {
 channel . ethBalances [ 0 ] += channel . ethBalances [ 2 ] ;
 channel . ethBalances [ 1 ] += channel . ethBalances [ 3 ] ;
 }
 else {
 require ( possibleTotalEthBeforeDeposit == totalEthDeposit ) ;
 }
 if ( possibleTotalTokenBeforeDeposit < totalTokenDeposit ) {
 channel . erc20Balances [ 0 ] += channel . erc20Balances [ 2 ] ;
 channel . erc20Balances [ 1 ] += channel . erc20Balances [ 3 ] ;
 }
 else {
 require ( possibleTotalTokenBeforeDeposit == totalTokenDeposit ) ;
 }
 Int ethbalanceA = channel . ethBalances [ 0 ] ;
 Int ethbalanceI = channel . ethBalances [ 1 ] ;
 Int tokenbalanceA = channel . erc20Balances [ 0 ] ;
 Int tokenbalanceI = channel . erc20Balances [ 1 ] ;
 channel . ethBalances [ 0 ] = 0 ;
 channel . ethBalances [ 1 ] = 0 ;
 channel . erc20Balances [ 0 ] = 0 ;
 channel . erc20Balances [ 1 ] = 0 ;
 if ( ethbalanceA != 0 || ethbalanceI != 0 ) {
 channel . partyAddresses [ 0 ] . transfer ( ethbalanceA ) ;
 channel . partyAddresses [ 1 ] . transfer ( ethbalanceI ) ;
 }
 if ( tokenbalanceA != 0 || tokenbalanceI != 0 ) {
 require ( channel . token . transfer ( channel . partyAddresses [ 0 ] , tokenbalanceA ) , "byzantineCloseChannel: token transfer failure" ) ;
 require ( channel . token . transfer ( channel . partyAddresses [ 1 ] , tokenbalanceI ) , "byzantineCloseChannel: token transfer failure" ) ;
 }
 channel . isOpen = false ;
 numChannels -- ;
 }
 <EOF>