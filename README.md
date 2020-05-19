# Tournament game based on P2P-multi-connections

This is incredibly good project for developing understanding of Multi-Threading

## Description

It's a console game where all players connected in a lobby should play against each other in a simple game:
Two players on the arena, randomly one of the player starts count down to some predetermined number (generated by the game earlier). And then each of both players increase the number by one and then send it to another player. The winner is the player who will increase the number to predermined one!  

## Implementation

The main idea of the project is to implement P2P connection between all of the players connected in one lobby using TCP client and server. In order to maintain this connection between many players, I use the system of so called Coorinator and obviously Threads. This is the user who contains the list with all players that are connected. If this player leaves game, the next player in the list becomes the Coordinator that updates info about users. You may connect not only to your friend (who isn't the coordinator), and he will send information about you to the coordinator.
