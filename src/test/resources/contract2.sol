contract myContract2 {
	address owner;
	function getI1() constant returns (string) {return "hello";}
	function getT() constant returns (bool) {return true;}
	function getM() constant returns (bool,string,uint) {return (true,"hello",34);}
	function getOwner() constant returns (address) {return owner;}
	function getArray() constant returns (uint[10] arr) {
		for(uint i = 0; i < 10; i++) {
			arr[i] = i;
		}
	}
}
