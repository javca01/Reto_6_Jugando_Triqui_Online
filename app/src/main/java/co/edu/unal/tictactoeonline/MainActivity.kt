package co.edu.unal.tictactoeonline

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import co.edu.unal.tictactoeonline.databinding.ActivityMainBinding
import kotlin.random.Random
import kotlin.random.nextInt

//VERSION MODIFICADA
class MainActivity : AppCompatActivity() {

    lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.playOfflineBtn.setOnClickListener {
            createOfflineGame()
        }

        binding.createOnlineGameBtn.setOnClickListener {
            createOnlineGame()
        }

        binding.joinOnlineGameBtn.setOnClickListener {
            joinOnlineGame()
        }

    }


    fun createOfflineGame() { // Inicia un juego en modo offline
        GameData.saveGameModel(
            GameModel(
                gameStatus = GameStatus.JOINED,
                gameId = "-1" // Modo de juego offline.
            )
        )
        startGame()
    }


    fun createOnlineGame(){ //Crea una sesión de juego online.
        GameData.myID = "X"
        GameData.saveGameModel(
            GameModel(
                gameStatus = GameStatus.CREATED,
                gameId = Random.nextInt(1000..9999).toString()
            )
        )
        startGame()
    }

    fun joinOnlineGame(){ //Valida en Frebase un código ingresado para iniciar un juego online.
        var gameId = binding.gameIdInput.text.toString()
        if(gameId.isEmpty()){
            binding.gameIdInput.setError("Ingresa el ID del juego.")
            return
        }
        GameData.myID = "O"
        Firebase.firestore.collection("games")
            .document(gameId)
            .get()
            .addOnSuccessListener {
                val model = it?.toObject(GameModel::class.java)
                if(model==null){
                    binding.gameIdInput.setError("Ingresa un ID de juego válido.")
                }else{
                    model.gameStatus = GameStatus.JOINED
                    GameData.saveGameModel(model)
                    startGame()
                }
            }

    }

    fun startGame(){ //Implementa la clase GameActivity, es decir, inicia un juego.
        startActivity(Intent(this,GameActivity::class.java))
    }



}
