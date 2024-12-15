package co.edu.unal.tictactoeonline

import kotlin.random.Random

data class GameModel (
    var gameId : String = "-1", //tipo del juego.
    var filledPos : MutableList<String> = mutableListOf("","","","","","","","",""), //elementos del tablero.
    var winner : String ="", //ganador.
    var gameStatus : GameStatus = GameStatus.CREATED,//estatus del juego.
    var currentPlayer : String = (arrayOf("X","O"))[Random.nextInt(2)]//jugador actual. Define aleatoriamente si el jugador es X o O.
)


enum class GameStatus{//lista con todos los posibles estados del juego.
    CREATED,
    JOINED,
    INPROGRESS,
    FINISHED
}