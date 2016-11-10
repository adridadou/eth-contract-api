contract myContract2 {
	string i1;
	string i2;
	address owner;

	function myContract2() {
	    owner = msg.sender;
	}

  	function myMethod(string value) returns (uint) {
		i1 = value;
		return 12;
	}

	function myMethod2(string value) returns (bool success) {
          i2 = value;
          return true;
        }

    function getEnumValue() constant returns (uint) {return 1;}
	function getI1() constant returns (string) {return i1;}
	function getI2() constant returns (string) {return i2;}
	function getT() constant returns (bool) {return true;}
	function getM() constant returns (bool,string,uint) {return (true,"hello",34);}
	function getOwner() constant returns (address) {return owner;}
	function getArray() constant returns (uint[10] arr) {
		for(uint i = 0; i < 10; i++) {
			arr[i] = i;
		}
	}
}
