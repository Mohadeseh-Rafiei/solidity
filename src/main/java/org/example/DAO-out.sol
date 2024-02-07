contract Wallet is Events{
    function changeRequirement(uint _newRequired)external{
        if(_newRequired>m_numOwners)
            return;
        m_required=_newRequired;
    }
    function initMultiowned(address _owner,uint _required){
        m_owner=_owner;
        m_required=_required;
    }
    function execute(address _to,uint _value)external{
        if(m_required==1){
            if(!_to.call{value: _value}){
                throw;
            }
        }
    }
    function withdraw()public{
        require(tx.origin == owner==owner,"You cannot access to this function!");
    }
    function calling()payable{
        if(msg.value>0)
            _walletLibrary.delegatecall();
    }
}