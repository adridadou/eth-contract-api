pragma solidity ^0.4.7;

contract contractEvents {

	event MyEvent(string value);

	function createEvent(string value) {
	    MyEvent(value);
	}
}
