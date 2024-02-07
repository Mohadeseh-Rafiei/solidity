contract MyContractisIERC20{
    mapping(address=>uint256)balances;
    mapping(address=>mapping(address=>uint256))allowed;
    uint256 totalSupply_=10ether;
    constructor(){
        balances[msg.sender]=totalSupply_;
    }
    function transfer(address receiver,uint256 numTokens)public override returns(bool){
        require(numTokens<=balances[msg.sender]);
        balances[msg.sender]=balances[msg.sender]-numTokens;
        balances[receiver]=balances[receiver]+numTokens;
        return true;
    }
    function approve(address delegate,uint256 numTokens)public override returns(bool){
        allowed[msg.sender][delegate]=numTokens;
        return true;
    }
    function transferFrom(address owner,address buyer,uint256 numTokens)public override returns(bool){
        require(numTokens<=balances[owner]);
        require(numTokens<=allowed[owner][msg.sender]);
        balances[owner]=balances[owner]-numTokens;
        allowed[owner][msg.sender]=allowed[owner][msg.sender]+numTokens;
        balances[buyer]=balances[buyer]+numTokens;
        return true;
    }
}