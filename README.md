# eth-contract-api
[![CircleCI](https://circleci.com/gh/adridadou/eth-contract-api/tree/develop.svg?style=svg)](https://circleci.com/gh/adridadou/eth-contract-api/tree/develop)
[![Coverage Status](https://coveralls.io/repos/github/adridadou/eth-contract-api/badge.svg?branch=develop)](https://coveralls.io/github/adridadou/eth-contract-api?branch=develop)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/3b1efe2be2094d8587e5dc22b8d4a00b)](https://www.codacy.com/app/Adridadou/eth-contract-api?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=adridadou/eth-contract-api&amp;utm_campaign=Badge_Grade)
[![Gitter](https://badges.gitter.im/eth-contract-api/Lobby.svg)](https://gitter.im/eth-contract-api/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)

EthereumJ native API to use and test smart contracts easily

#Getting started
Here is a quick guide on how to install and use this library
##Installation
You can find the application in its bintray repo.
To add the repository, add the following to your pom.xml
````
     <repository>
        <id>bintray</id>
        <url>https://dl.bintray.com/cubefriendly/maven/</url>
    </repository>
````

Then you are ready to add eth-contract-api as a dependency
````
<dependency>
    <groupId>org.adridadou</groupId>
    <artifactId>eth-contract-api</artifactId>
    <version>[put the latest version here]</version>
</dependency>
````

##Setup
Now that you have added the library as a dependency, it is time to set it up.

###EthereumFacadeProvider
eth-contract-api abstracts away how and which network you are connecting to.
You shouldn't see in your code whether you have a local node or using RPC, if the network is 
the main network, testnet, a private one or even a mocked network for unit testing.

In order to configure all that, you need to use an EthereumFacadeProvider. Several ones exist
to make it easier to start.

#### AccountProvider 
This class is used to get an account object.
It can create an account from a String (seed) or from a File (keystore)

####EthereumFacadeProvider
This is the provider used to connect through EthereumJ. 
The different pre-defined configurations can be found at org.adridadou.ethereum.ethj.EthereumJConfigs
You can use the function "from" that takes a configuration.
This returns a Provider Builder. You can then extend the configuration. This is useful when you want to connect to Ropsten but with fast sync (for example)

####StandaloneEthereumFacadeProvider
This is a mocked blockchain to have fast testing. Not all the functionalities
are available here but this is a great way to write tests against
your smart contracts that run fast.

If you feel that something is missing, please create an issue

####RpcEthereumFacadeProvider
This is the provider used to connect to a remote node through RPC.
It uses the library web3j. The accounts are always handled locally, i.e.
the accounts on the node are never used

####InfuraRopstenEthereumFacadeProvider
This provider is used to connect to Ropsten through Infura.
All you need is to provide your api key and you are good to go

####InfuraMainEthereumFacadeProvider
This provider is used to connect to the main network through Infura.
All you need is to provide your api key and you are good to go

###How to publish a smart contract
How can you publish a smart contract to the network? It is pretty simple,
you need to use the EthereumFacade and use the method publish:

First you need to read the source. The source is represented by 
 the type SoliditySource. The class has 3 static methods called `from` that takes one parameter:
 * A string - the actual source
 * A file - the file where the source code is
 * A InputStream - A stream to read the source
 
 Here is an example of reading a source code from a file:
````
        SoliditySource contract = SoliditySource.from(new File(this.getClass().getResource("/contract.sol").toURI()));
````

Then you can pass the new source object to publish it to the network:
````
        CompletableFuture<EthAddress> futureAddress = ethereum.publishContract(contract, "myContract2", account);
````

The method takes as parameters:
* The source object
* The name of the smart contract to publish. This is necessary because there can be more than one in a source file
* The EthAccount that deploys the smart contract

The return value, a Future EthAddress will be resolved as soon as the contract creation has been mined and an address is available.

You can get your EthAccount by using your EthereumFacadeProvider that always has a method `getLockedAccount`. This will return a SecureKey object.
This secure key object has a method `decode` that takes the keystore password as a parameter. If your key comes from a brain wallet (from a string), then any password will do. Simply call the method with an empty String and enjoy!

### How to use a smart contract in my application?
Now how do I get access to a smart contract from my code?
First you need to create an interface that represents your smart contract.

Let's imagine this smart contract:
````
contract myContract2 {
	string i1;
	string i2;
	address owner;

	function myContract2() {
	    owner = msg.account;
	}

  	function myMethod(string value) returns (uint) {
		i1 = value;
		return 12;
	}

	function myMethod2(string value) {
      i2 = value;
    }

    function getEnumValue() constant returns (uint) {return 1;}
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
````
This smart contract doesn't do much, fair enough :) But this is a perfect example to explain all the different things to take
into account when creating an interface.

````
    public interface MyContractInterface {
        CompletableFuture<Integer> myMethod(String value);
     	CompletableFuture<Void> myMethod2(String value);
        MyEnum getEnumValue();
     	String getI1();
     	Boolean getT();
     	MyReturnValue getM();
     	EthAddress getOwner();
     	List<BigInteger> getArray();
    }
````

The rules are the following:
* If the smart contract function is not a constant, the corresponding method in the interface should return a CompletableFuture<ReturnType>
* If the smart contract function is constant, return the type directly
* uint can be converted to Integer, Long and BigInteger
* If your function returns more than one value, you have to create a class representing the tuple with a constructor matching the return types
* If a function doesn't return anything, you can either return nothing (void) and then the call will block or you can return CompletableFuture<Void> to have a non blocking call
* If you want to match a return type to an Enum, the value represents the ordinal position of the value
* Arrays can be converted to Java arrays, List and Set
* the address type converts to EthAddress

You can implement your own converter if needed. All you have to do is create a class that implements `InputTypeConverter` if you want to convert a parameter and `OutputTypeConverter` when you want to convert a return value.
Then you can register the converter by using the methods `addInputHandlers` or `addOutputHandlers` in your EthereumFacade object.
 
####Create a proxy object
To interface to a smart contract, use the method `createContractProxy` from EthereumFacade.
This method can take a source file object + a contract name or an ABI object. 
Then the other parameters are:
* The address of the smart contract
* The account to use when calling the functions
* The interface class used to interface with the smart contract

````
public <T> T createContractProxy(SoliditySource code, String contractName, EthAddress address, EthAccount account, Class<T> contractInterface)
public <T> T createContractProxy(ContractAbi abi, EthAddress address, EthAccount account, Class<T> contractInterface)
````

The interface is then validated against the ABI to make sure that they are compatible.

### Other methods in EthereumFacade
EthereumFacade can be used to use other features of Ethereum

####addressExists(EthAddress address)
Checks whether this address exists in the EVM

####getBalance(EthAddress address)
Returns the balance of the current address. The address can be a smart contract or an account

####sendEther(EthAccount from, EthAddress to, EthValue amount)
This sends a certain amount of Ether from the given account to the given address

####getNonce(EthAddress address)
Gives you the Nonce of a given address

####events()
This method returns the EventHandler object.
This is the to go object is you want to register to certain events. The events can be blockchain specific events
(such as a new block, or the chain is finally in sync) or to listen to Solidity events

##Testing
eth-contract-lib has two helper classes to write tests. Each one is designed for a different kind of tests.

###StandaloneEthereumFacade
As explained before, this creates a mocked version of the blockchain. This provider is perfect if you want to test
the intergration between your smart contract and your interface. This means that you won't have any events and most of the functionalities
beyond calling a smart contract (constant or with a transaction) won't work.
The advantage of this provider is that the tests run fast

###PrivateNetworkProvider
This creates a private network that runs with only one node.
This is perfect to create an integration tests with an actual EthereumNode. 

You can set the balances of different accounts by using the PrivateNetworkConfig. 

Beware that the private networks need to generate a dag.dat file that is about 1Gb in size.
The library caches the file in cachedDag/ directory but this may be an issue in your CI. 
If you are using a CI, I suggest that you configure it to cache the directory cachedDag


# Other questions?
If you have any further question please join gitter and drop me a word. I promise I'll answer as soon as I can!

