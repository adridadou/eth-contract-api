# eth-contract-api
[![CircleCI](https://circleci.com/gh/adridadou/eth-contract-api/tree/develop.svg?style=svg)](https://circleci.com/gh/adridadou/eth-contract-api/tree/develop)
[![Coverage Status](https://coveralls.io/repos/github/adridadou/eth-contract-api/badge.svg?branch=develop)](https://coveralls.io/github/adridadou/eth-contract-api?branch=develop)
[![Gitter](https://badges.gitter.im/eth-contract-api/Lobby.svg)](https://gitter.im/eth-contract-api/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)

EthereumJ native API or how to call contracts easily and in type safely.

If you have any question please read the wiki https://github.com/adridadou/eth-contract-api/wiki

The goal of this project is to ease the integration of Ethereum in a Java project.
It should be easy to:
* (OK) - Easy configuration of the network and keypair use
* (OK) - Create an interface for a smart contract
* (OK) - Have type safety in regards of input and output values
* (OK) - Easy transaction creation
* (OK) - Easy synchronization when creating a transaction
* (in progress) - Documentation
* (OK) - Transaction creation returns Future, simple calls returns the value itself
* (TODO) - Data inspection in a smart contract
