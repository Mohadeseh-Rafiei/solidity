contract DosAuction{
addresshighestBidder=0x0000000000000000000000000000000000000000;uint256highestBid=0;functionbid()publicpayable{require(msg.value>highestBid,"Need to be higher than highest bid");if(highestBidder==0x0000000000000000000000000000000000000000){highestBidder=msg.sender;highestBid=msg.value;}else{require(,"Failed to send Ether");highestBidder=msg.sender;highestBid=msg.value;}}}<EOF>