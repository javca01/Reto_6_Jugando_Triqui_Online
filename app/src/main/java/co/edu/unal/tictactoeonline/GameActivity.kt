package co.edu.unal.tictactoeonline

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import co.edu.unal.tictactoeonline.databinding.ActivityGameBinding

class GameActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var binding: ActivityGameBinding

    private var gameModel: GameModel? = null
    private var humanScore = 0
    private var machineScore = 0
    private var ties = 0
    private var player1Score = 0
    private var player2Score = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        GameData.fetchGameModel()

        // Initialize UI listeners
        arrayOf(
            binding.btn0, binding.btn1, binding.btn2,
            binding.btn3, binding.btn4, binding.btn5,
            binding.btn6, binding.btn7, binding.btn8
        ).forEach { it.setOnClickListener(this) }

        binding.startGameBtn.setOnClickListener {
            startGame()
        }

        GameData.gameModel.observe(this) {
            gameModel = it
            setUI()
        }
    }

    private fun setUI() {
        gameModel?.apply {
            // Update board positions
            binding.btn0.text = filledPos[0]
            binding.btn1.text = filledPos[1]
            binding.btn2.text = filledPos[2]
            binding.btn3.text = filledPos[3]
            binding.btn4.text = filledPos[4]
            binding.btn5.text = filledPos[5]
            binding.btn6.text = filledPos[6]
            binding.btn7.text = filledPos[7]
            binding.btn8.text = filledPos[8]

            // Update game status text
            binding.gameStatusText.text = when (gameStatus) {
                GameStatus.CREATED -> {
                    binding.startGameBtn.visibility = View.INVISIBLE
                    "Game ID: $gameId"
                }
                GameStatus.JOINED -> {
                    "Inicia el Juego."
                }
                GameStatus.INPROGRESS -> {
                    binding.startGameBtn.visibility = View.INVISIBLE
                    if (GameData.myID == currentPlayer) "Tu turno" else "Juega $currentPlayer"
                }
                GameStatus.FINISHED -> {
                    if (winner.isNotEmpty()) {
                        if (winner == GameData.myID) {
                            "¡Tú ganas!"
                        } else {
                            "$winner gana!"
                        }
                    } else {
                        "Empate."
                    }
                }
            }

            // Update scores based on mode
            //Actualización de los puntajes basados en el modo.
            if (gameId == "-1") { // Modo Offline.
                binding.scoreText.text = "Tú: $humanScore | Máquina: $machineScore | Empate: $ties"
            } else { // Online mode
                binding.scoreText.text = "Jugadr_1: $player1Score | Jugadr_2: $player2Score | Empate: $ties"
            }
        }
    }

    private fun startGame() {
        gameModel?.apply {
            updateGameData(
                GameModel(
                    gameId = gameId,
                    gameStatus = GameStatus.INPROGRESS
                )
            )
        }
    }
    fun updateGameData(model : GameModel){ //Actualiza los datos del juego.
        GameData.saveGameModel(model)
        binding.startGameBtn.visibility = View.VISIBLE //Regenera el botón Iniciar juego, luego de terminar una partida.
    }

    private fun updateScores() {
        gameModel?.apply {
            if (gameId == "-1") { // Modo Offline
                when (winner) {
                    "X" -> humanScore++
                    "O" -> machineScore++
                    else -> ties++
                }
            } else { // Online mode
                when (winner) {
                    "X" -> player1Score++
                    "O" -> player2Score++
                    else -> ties++
                }
            }
        }
    }

    private fun checkForWinner() {
        val winningPos = arrayOf(
            intArrayOf(0, 1, 2),
            intArrayOf(3, 4, 5),
            intArrayOf(6, 7, 8),
            intArrayOf(0, 3, 6),
            intArrayOf(1, 4, 7),
            intArrayOf(2, 5, 8),
            intArrayOf(0, 4, 8),
            intArrayOf(2, 4, 6),
        )

        gameModel?.apply {
            for (i in winningPos) {
                if (
                    filledPos[i[0]] == filledPos[i[1]] &&
                    filledPos[i[1]] == filledPos[i[2]] &&
                    filledPos[i[0]].isNotEmpty()
                ) {
                    gameStatus = GameStatus.FINISHED
                    winner = filledPos[i[0]]
                    updateScores()
                    setUI()
                    return
                }
            }

            if (filledPos.none { it.isEmpty() }) { // Check for tie
                gameStatus = GameStatus.FINISHED
                winner = ""
                ties++
                setUI()
            }

            updateGameData(this)
        }
    }

    override fun onClick(v: View?) {
        gameModel?.apply {
            if (gameStatus != GameStatus.INPROGRESS) {
                Toast.makeText(applicationContext, "Juego no iniciado.", Toast.LENGTH_SHORT).show()
                return
            }

            if (gameId != "-1" && currentPlayer != GameData.myID) {
                Toast.makeText(applicationContext, "No es tu turno.", Toast.LENGTH_SHORT).show()
                return
            }

            val clickedPos = (v?.tag as String).toInt()
            if (filledPos[clickedPos].isEmpty()) {
                filledPos[clickedPos] = currentPlayer
                currentPlayer = if (currentPlayer == "X") "O" else "X"
                checkForWinner()
                if (gameId == "-1" && gameStatus == GameStatus.INPROGRESS) { // Mod Offline.
                    makeComputerMove()
                }
                updateGameData(this)
            }
        }
    }
    private fun makeComputerMove() {
        gameModel?.apply {
            val computerMove = getComputerMove(this) // Pasa `gameModel` como parámetro.
            filledPos[computerMove] = currentPlayer
            currentPlayer = if (currentPlayer == "X") "O" else "X"
            checkForWinner()
            updateGameData(this)
        }
    }
    // Actualiza "getComputerMove" para aceptar la instancia "gameModel"
    private fun getComputerMove(gameModel: GameModel): Int {
        return getWinningMove(gameModel)
            ?: getBlockingMove(gameModel)
            ?: findBestMove(gameModel)
            ?: getRandomMove(gameModel)
    }

    // Actualzia "getWinningMove" para aceptar la instancia "gameModel"
    private fun getWinningMove(gameModel: GameModel): Int? {
        val player = gameModel.currentPlayer
        return findMove(gameModel, player) // Delega "findMove con el jugador actual.
    }


    // Actualiza el metodo "getBlockingMove" para aceptar la instancia "gameModel"
    private fun getBlockingMove(gameModel: GameModel): Int? {
        val opponent = if (gameModel.currentPlayer == "X") "O" else "X"
        return findMove(gameModel, opponent) // Delega "findMove" al oponente.
    }

    // Encuentra un ganador o bloquea el movimiento del oponente.
    private fun findMove(gameModel: GameModel, player: String): Int? {
        val winningCombinations = arrayOf(
            intArrayOf(0, 1, 2),
            intArrayOf(3, 4, 5),
            intArrayOf(6, 7, 8),
            intArrayOf(0, 3, 6),
            intArrayOf(1, 4, 7),
            intArrayOf(2, 5, 8),
            intArrayOf(0, 4, 8),
            intArrayOf(2, 4, 6)
        )

        for (combination in winningCombinations) {
            val (a, b, c) = combination
            val positions = gameModel.filledPos
            if (positions[a] == player && positions[b] == player && positions[c].isEmpty()) {
                return c
            }
            if (positions[a] == player && positions[c] == player && positions[b].isEmpty()) {
                return b
            }
            if (positions[b] == player && positions[c] == player && positions[a].isEmpty()) {
                return a
            }
        }
        return null
    }

    // Actualiza el metodo "getRandomMove" para aceptar la instancia "gameModel".
    private fun getRandomMove(gameModel: GameModel): Int {
        val emptyPositions = gameModel.filledPos.mapIndexedNotNull { index, value ->
            if (value.isEmpty()) index else null
        }
        return emptyPositions.random()
    }


    // Actualiza "findBestMove" para aceptar la instancia "gameModel"
    private fun findBestMove(gameModel: GameModel): Int? {
        val preferredMoves = listOf(4, 0, 2, 6, 8, 1, 3, 5, 7) // Centro, esquinas y bordes.
        for (move in preferredMoves) {
            if (gameModel.filledPos[move].isEmpty()) {
                return move
            }
        }
        return null
    }

}
