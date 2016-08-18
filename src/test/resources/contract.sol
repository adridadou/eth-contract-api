contract myContract2 {
	string i1;
	address owner;
  	function myMethod(string value) returns (uint) {
		i1 = value;
		owner = msg.sender;
		return 12;
	}
	function getI1() constant returns (string) {return i1;}
	function getT() constant returns (bool) {return true;}
	function getM() constant returns (bool,string,uint) {return (true,"hello",34);}
	function getOwner() constant returns (address) {return owner;}
	function getArray() constant returns (uint[10] arr) {
		for(uint i = 0; i < 10; i++) {
			arr[i] = i;
		}
	}
}
