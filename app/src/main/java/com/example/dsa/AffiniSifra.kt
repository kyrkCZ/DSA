package com.example.Cryptology

// ...
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement.Absolute.SpaceBetween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import com.example.dsa.R
import java.text.Normalizer
import java.util.Locale



private fun displayCustomToast(inflater: LayoutInflater, context: Context, parentView: View, text: String) {

    val toast = Toast(context)
    toast.duration = Toast.LENGTH_SHORT
    toast.view = layout
    toast.setText(text)
}

val alphabet = ('a'..'z').joinToString("") + ('0'..'9').joinToString("")
val m = alphabet.length
val alphabetArray = alphabet.toCharArray()
var cipheredAlphabet = alphabetArray
const val mezera = "xmezerax"

fun removeDiacritics(input: String): String {
    val normalizedString = Normalizer.normalize(input, Normalizer.Form.NFD)
    return normalizedString.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
}

fun mod(int: Int, mod: Int): Int {
    var result = int % mod
    if (result < 0) {
        result += mod
    }
    return result
}

fun encode(input: String, a: Int, b: Int): String {
    var counter = 1
    var output = ""
    for (char in input){
        output += cipherChar(char, a, b).toString()
        if(counter==5) {

            output+=" "
            counter=0
        }
        counter++
    }
    return output.uppercase(Locale.getDefault())
}

fun cipherChar(char: Char, a: Int, b: Int): Char {
    val defaultIndex = alphabet.indexOf(char.lowercaseChar())
    val index = mod((a * defaultIndex + b), m)
    Log.d("cipherChar", "char: $char, index: $index")
    Log.d("cipherChar2", "alphabetindexof: $defaultIndex, a: $a, b: $b")
    return alphabetArray[index]
}

fun decode(input: String, a: Int, b: Int): String {
    val mmiA = mmi(a, m) // Modular Multiplicative Inverse of 'a' modulo 'm'
    val decoded = StringBuilder()

    val inputWithoutSpaces = input.replace(" ", "") // Remove spaces

    for (char in inputWithoutSpaces) {
        if (char.isLetterOrDigit()) {
            val index = alphabet.indexOf(char.toLowerCase())
            // Decoding using the formula: y = a^(-1) * (x - b) mod m
            val decodedIndex = (mmiA * (index - b + m)) % m
            decoded.append(if (decodedIndex < alphabet.length) alphabetArray[decodedIndex] else (decodedIndex - alphabet.length + '0'.toInt()).toChar())
        }
    }

    return decoded.toString()
}

fun mmi(a: Int, b: Int): Int = (1 until b).dropWhile { (a * it) % b != 1 }.firstOrNull() ?: 1

fun gcd(a: Int, b: Int): Int{
    var gcd = 1
    generateSequence(1) { it + 1 }
        .takeWhile { it <= a && it <= b }
        .forEach { if (a % it == 0 && b % it == 0) gcd = it }
    return gcd
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun SimpleOutlinedTextFieldSample() {
    var text by remember { mutableStateOf("") }
    var texta by remember { mutableStateOf("") }
    var textb by remember { mutableStateOf("") }
    var textFormatedPrivate by remember { mutableStateOf("Formatted text:") }
    var textFormated by remember { mutableStateOf("") }
    var ciphredText by remember { mutableStateOf("") }
    var cypheredAlphabet by remember { mutableStateOf("") }
    var actionCompleted by remember { mutableStateOf(false) }


    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
        val context = LocalContext.current
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Plain text"
            ) }
        )
        Column {
            OutlinedTextField(
                value = texta,
                onValueChange = { if (it.isDigitsOnly()) texta = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                label = { Text("A") }
            )
            OutlinedTextField(
                value = textb,
                onValueChange = { if (it.isDigitsOnly()) textb = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                label = { Text("B") }
            )
        }

        Row (horizontalArrangement = SpaceBetween){
            Text(text = textFormated, fontWeight = FontWeight.Bold)
        }
        Button(onClick = {
            try {
                texta.toInt()
                textb.toInt()
            } catch (e: Exception) {
                texta = "1"
                textb = "1"
            }
            if(gcd(texta.toInt(),m)==1) {
                cypheredAlphabet = encode(alphabet, texta.toInt(), textb.toInt())
                cipheredAlphabet = cypheredAlphabet.replace(" ", "").toCharArray()
                actionCompleted = true
            }
            if (actionCompleted) {

                actionCompleted = false
            }else{
                Toast.makeText(
                    context,
                    "GCD not OK",
                    Toast.LENGTH_LONG
                ).show()
            }
        }) {
            Text(text = "Check GCD")
        }
        Button(onClick = {
            try {
                texta.toInt()
                textb.toInt()
            } catch (e: Exception) {
                texta = "1"
                textb = "1"
            }
            //textFormatedPrivate = removeDiacritics(text).lowercase(Locale.getDefault())
            //    .replace(" ", mezera).replace(Regex("[^A-Za-z0-9]"), "")
            textFormated = "Formatted text: " + text
                .replace(" ", mezera)
                .replace(Regex("[^A-Za-z0-9]"), "")
                .uppercase(Locale.ROOT)
            ciphredText = encode(textFormated.drop(16), texta.toInt(), textb.toInt())
        }) {
            Text(text = "Cipher")
            Modifier.fillMaxWidth(0f)
        }
        OutlinedTextField(
            value =ciphredText,
            onValueChange = { ciphredText = it },
            label = { Text("Ciphered text") }
        )
        Spacer(modifier = Modifier.padding(10.dp))
        Button(onClick = {
            try {
                texta.toInt()
                textb.toInt()
            } catch (e: Exception) {
                texta = "1"
                textb = "1"
            }
            text = if (ciphredText.isNotEmpty()&& ciphredText.isNotBlank())
                decode(ciphredText, texta.toInt(), textb.toInt()).replace(" ","").replace(mezera, " ")
                    .uppercase()
            else
                "Ciphered text is empty"

        }) {
            Text(text = "Decipher")
        }
        Image(
            painter = painterResource(id = R.drawable.png_transparent_encryption_cryptography_computer_icons_computer_network_key_computer_network_text_objects_thumbnail),
            contentDescription = "Logo",
            modifier = Modifier
                .padding(10.dp)
        )
        LazyColumn(content = {
            val minArraySize = minOf(alphabetArray.size, cipheredAlphabet.size)

            for (i in 0 until minArraySize) {
                item(i) {
                    Text(text = "${alphabetArray[i]} -> ${cipheredAlphabet[i].lowercase()}")
                }
            }
        })
    }
}

