package com.example.dsa

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.text.Normalizer

var czechAlphabet = false
var mezera = "XMEZERAX"


class ADVFGX : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AffineCipherActivity()
                }
            }
        }
    }
}

fun removeDiacritics(input: String): String {
    val normalizedString = Normalizer.normalize(input, Normalizer.Form.NFD)
    return normalizedString.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
}

fun replacePlainTextWithMatrixValues(plainText: String, matrix: Array<Array<Pair<Char, String>>>): String {
    val formattedText = StringBuilder()

    for (char in plainText) {
        if (char.isLetterOrDigit()) {
            val value = findValueInMatrix(char, matrix)
            formattedText.append("$value ")
        }
    }

    return formattedText.toString().trim()
}

fun findValueInMatrix(char: Char, matrix: Array<Array<Pair<Char, String>>>): String {
    for (row in matrix) {
        for (pair in row) {
            if (pair.first == char) {
                return pair.second
            }
        }
    }
    return "" // Return an empty string if the character is not found in the matrix.
}

fun indexStringToPairs(input: String): Array<Pair<Char, Int>> {
    return input.mapIndexed { index, char ->
        Pair(char, index)
    }.toTypedArray()
}

fun cipherADFGX(text: String, keyword: String): String {
    val textList = text.replace(" ", "").toList()
    val keywordArrayPair = indexStringToPairs(keyword)
    var keypair = mutableListOf<Pair<Pair<Char, Int>, String>>()

    for(i in 0 until textList.size) {
        keypair.add(Pair(keywordArrayPair[i%keyword.length], textList[i].toString()))
    }

    keypair.sortWith(compareBy({ it.first.first }, { it.first.second }))

    val cipheredText = StringBuilder()

    for (pair in keypair) {
        cipheredText.append(pair.second)
    }

    return cipheredText.toString().trim()
}

fun replaceDuplicateCharsWithNumericalSuffix(input: String): String {
    val charCountMap = mutableMapOf<Char, Int>()
    val result = StringBuilder()

    for (char in input) {
        if (charCountMap.containsKey(char)) {
            // Character is already encountered, increment the count
            val count = charCountMap[char]!! + 1
            charCountMap[char] = count
            result.append("$char$count")
        } else {
            // First occurrence of the character
            charCountMap[char] = 1
            result.append(char)
        }
    }

    return result.toString()
}

fun decipherADFGX(cipheredText: String, keyword: String): String {
    val keywordArrayPair = indexStringToPairs(keyword)
    val sortedKeyWordArrayPair = keywordArrayPair.sortedWith(compareBy({ it.first }, { it.second }))

    val cipheredTextList = splitStringByXChars(cipheredText, cipheredText.length / sortedKeyWordArrayPair.size)
    Log.d("ciphered", cipheredTextList.toString())

    val keywordMap = keywordArrayPair.mapIndexed { index, pair -> pair to index }.toMap()
    Log.d("keywordMap", keywordMap.toString())

    val pairs = mutableListOf<Pair<Pair<Char, Int>, String>>()
    for (i in 0 until sortedKeyWordArrayPair.size) {
        pairs.add(Pair(sortedKeyWordArrayPair[i], cipheredTextList[i]))
    }

    val sortedPairsByKey = pairs.sortedBy { keywordMap[it.first] }
    Log.d("sorted", sortedPairsByKey.toString())
    val decipheredText = buildStringFromPairs(sortedPairsByKey)
    Log.d("deciphered", decipheredText)
    val slicedDecipheredText = insertSpaceEveryNChars(decipheredText, 2)

    return slicedDecipheredText.trim()
}

fun insertSpaceEveryNChars(input: String, n: Int): String {
    val result = StringBuilder()

    for (i in 0 until input.length step n) {
        val endIndex = (i + n).coerceAtMost(input.length)
        result.append(input.substring(i, endIndex))
        if (endIndex < input.length) {
            result.append(" ") // Insert a space after every 2 characters
        }
    }

    return result.toString()
}


fun buildStringFromPairs(pairs: List<Pair<Pair<Char, Int>, String>>): String {
    val maxLength = pairs.maxByOrNull { it.second.length }?.second?.length ?: 0
    val stringBuilder = StringBuilder()

    for (i in 0 until maxLength) {
        for (pair in pairs) {
            val string = pair.second
            if (i < string.length) {
                stringBuilder.append(string[i])
            }
        }
    }

    return stringBuilder.toString()
}

fun replaceStringWithMatrixValues(input: String, matrix: Array<Pair<Char, String>>): String {
    val formattedText = StringBuilder()

    val splitText = splitStringByXChars(input, 2)

    for (string in splitText) {
        if (string.length == 2) {
            val char = matrix.find { it.second == string }?.first
            if (char != null) {
                formattedText.append(char)
            } else {
                formattedText.append(string) // If not found in the matrix, keep the original string.
            }
        } else {
            formattedText.append(string) // Keep any remaining characters that are not 2 characters long.
        }
    }

    return formattedText.toString().trim()
}



fun splitStringByXChars(input: String, chunkSize: Int): List<String> {
    require(chunkSize > 0) { "Chunk size must be greater than 0" }
    val result = mutableListOf<String>()
    var currentIndex = 0

    while (currentIndex < input.length) {
        val endIndex = (currentIndex + chunkSize).coerceAtMost(input.length)
        result.add(input.substring(currentIndex, endIndex))
        currentIndex = endIndex
    }

    return result
}

fun generateRandomizedAlphabetMatrix5x5(): Array<Array<Pair<Char, String>>> {
    // Define the alphabet
    val standardAlphabet = "ABCDEFGHIKLMNOPQRSTUVWXYZ"
    val alphabet = if (czechAlphabet) "ABCDEFGHIJKLMNOPQRSTUVXYZ" else standardAlphabet

    // Shuffle the alphabet to randomize it
    val shuffledAlphabet = alphabet.toCharArray().toMutableList().shuffled().joinToString("")
    Log.d("shuffled", shuffledAlphabet)
    // Initialize the 5x5 matrix
    val matrix = Array(5) { i ->
        Array(5) { j ->
            val currentChar = shuffledAlphabet[i * 5 + j]
            val rowIndex = "ADFGX"[i]
            val colIndex = "ADFGX"[j]
            Pair(currentChar, "$rowIndex$colIndex")
        }
    }
    return matrix
}

fun generateRandomizedAlphabetMatrix6x6(): Array<Array<Pair<Char, String>>> {
    // Define the alphabet
    val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    Log.d("alphabetLength", alphabet.length.toString())
    if (alphabet.length != 36) {
        throw IllegalArgumentException("Alphabet must have exactly 36 characters")
    }

    // Shuffle the alphabet to randomize it
    val shuffledAlphabet = alphabet.toCharArray().toMutableList().shuffled().joinToString("")

    val matrix = Array(6) { i ->
        Array(6) { j ->
            val rowIndex = "ADFGVX"[i]
            val colIndex = "ADFGVX"[j]
            val char = shuffledAlphabet[i * 6 + j]
            Pair(char, "$rowIndex$colIndex")
        }
    }

    return matrix
}

@Composable
fun ADVFGXActivity(context: Context) {
    var activeCipher by remember { mutableStateOf("ADFGX") }

    Column {
        NavigationBar(onCipherSelected = { cipher ->
            when (cipher) {
                "ADFGX" -> {

                }
                "ADFG(V)X" -> {

                }
            }
            activeCipher = cipher
        })

        when (activeCipher) {
            "ADFGX" -> ADFGXCipher()
            "ADFG(V)X" -> ADFGVXCipher()
        }
    }
}

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ADFGVXCipher() {
    var plainText by remember { mutableStateOf("") }
    var key by remember { mutableStateOf("") }
    var textFormated by remember { mutableStateOf("") }
    var textProcessed by remember { mutableStateOf("") }
    var ciphredText by remember { mutableStateOf("") }
    var decipheredText by remember { mutableStateOf("") }
    var plaindecipheredText by remember { mutableStateOf("Plain deciphered text: ") }
    var showDialog by remember { mutableStateOf(false) }
    var alphabetMatrix by remember { mutableStateOf(generateRandomizedAlphabetMatrix6x6()) }

    Box(modifier = Modifier
        .fillMaxSize()
        ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val context = LocalContext.current
            OutlinedTextField(
                value = plainText,
                onValueChange = { plainText = it },
                label = {
                    Text(
                        "Plain text"
                    )
                }
            )
            Column {
                OutlinedTextField(
                    value = key,
                    onValueChange = {
                        if (key.length > 35) key =
                            it.drop(1).uppercase().filter { it.isLetter() } else key =
                            it.uppercase().filter { it.isLetter() }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    label = { Text("KeyWord") },
                )
            }

            Row(horizontalArrangement = Arrangement.Absolute.SpaceBetween) {
                Text(text = textFormated, fontWeight = FontWeight.Bold)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = {
                    key = replaceDuplicateCharsWithNumericalSuffix(key)
                }) {
                    Text(text = "Check KEY")
                }

            }

            Button(onClick = {
                if(czechAlphabet) plainText = plainText.uppercase()
                plainText = removeDiacritics(plainText).uppercase()
                textFormated = replacePlainTextWithMatrixValues(plainText,alphabetMatrix)
                Log.d("textFormated", textFormated)
                ciphredText = try {
                    cipherADFGX(textFormated, key)
                } catch (e: Exception) {
                    Toast.makeText(context, "Key is invalid", Toast.LENGTH_SHORT).show()
                    ""
                }
            }) {
                Text(text = "Cipher")
                Modifier.fillMaxWidth(0f)
            }
            OutlinedTextField(
                value = ciphredText,
                onValueChange = { ciphredText = it },
                label = { Text("Ciphered text") }
            )
            Button(onClick = {
                plaindecipheredText += try {
                    decipherADFGX(ciphredText, key)
                } catch (e: Exception) {
                    Toast.makeText(context, "Key is invalid", Toast.LENGTH_SHORT).show()
                    ""
                }

            }) {
                Text(text = "Decipher")
            }
            Text(text = plaindecipheredText, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = decipheredText,
                onValueChange = { decipheredText = it },
                label = { Text("Deciphered text") })
            Button(onClick = {
                plaindecipheredText += try {
                    decipherADFGX(ciphredText, key)
                } catch (e: Exception) {
                    Toast.makeText(context, "Key is invalid", Toast.LENGTH_SHORT).show()
                    ""
                }
                Log.d("dataFromMetrix", extractDataFromMatrix5x5(alphabetMatrix).toString())
                val datafromMetrix = createMatrixPairs(extractDataFromMatrix5x5(alphabetMatrix))
                Log.d("dataFromMetrixPair", datafromMetrix[0].toString())
                val plainDecipheredText = plaindecipheredText.drop(22)
                Log.d("plainDecipheredText", plainDecipheredText)
                decipheredText = replaceStringWithMatrixValues(plainDecipheredText.replace(" ",""), datafromMetrix)
                Log.d("decipheredText", decipheredText)
            }) {
                Text(text = "Generate random matrix")
            }
            MatrixUI6x6(matrix = alphabetMatrix)
            if (showDialog) {
                AlertDialogExample(
                    onDismissRequest = { showDialog = false },
                    onConfirmation = {
                        // Handle confirmation logic here
                        showDialog = false
                    },
                    dialogTitle = "Špatný klíč",
                    dialogText = "Klíč musí být delší než 10 znaků a musí obsahovat pouze unikátní znaky.",
                    icon = Icons.Default.Warning // Use the appropriate icon
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ADFGXCipher() {
    var plainText by remember { mutableStateOf("") }
    var key by remember { mutableStateOf("") }
    var textFormated by remember { mutableStateOf("") }
    var textProcessed by remember { mutableStateOf("") }
    var ciphredText by remember { mutableStateOf("") }
    var decipheredText by remember { mutableStateOf("") }
    var plaindecipheredText by remember { mutableStateOf("Plain deciphered text: ") }
    var showDialog by remember { mutableStateOf(false) }
    var alphabetMatrix by remember { mutableStateOf(generateRandomizedAlphabetMatrix5x5()) }
    Box(modifier = Modifier
        .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val context = LocalContext.current
            OutlinedTextField(
                value = plainText,
                onValueChange = { plainText = it },
                label = {
                    Text(
                        "Plain text"
                    )
                }
            )
            Column {
                OutlinedTextField(
                    value = key,
                    onValueChange = {
                        if (key.length > 25) key =
                            it.drop(1).uppercase().filter { it.isLetter() } else key =
                            it.uppercase().filter { it.isLetter() }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    label = { Text("KeyWord") },
                )
            }

            Row(horizontalArrangement = Arrangement.Absolute.SpaceBetween) {
                Text(text = textFormated, fontWeight = FontWeight.Bold)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = {
                    key = replaceDuplicateCharsWithNumericalSuffix(key)
                }) {
                    Text(text = "Check KEY")
                }
                Text(
                    modifier = Modifier.padding(10.dp),
                    textAlign = TextAlign.Center,
                    text = "Eng"
                )

                Switch(
                    checked = czechAlphabet,
                    onCheckedChange = {
                        czechAlphabet = it
                    }

                )
                Text(
                    modifier = Modifier.padding(10.dp),
                    textAlign = TextAlign.Center,
                    text = "Cz"
                )
            }

            Button(onClick = {
                if(czechAlphabet) plainText = plainText.uppercase().replace("W","V").replace(" ", mezera) else plainText = plainText.uppercase().replace("J","I").replace(" ", mezera)
                textFormated = replacePlainTextWithMatrixValues(plainText,alphabetMatrix)
                ciphredText = try {
                    cipherADFGX(textFormated, key)
                } catch (e: Exception) {
                    Toast.makeText(context, "Key is invalid", Toast.LENGTH_SHORT).show()
                    ""
                }
                //ciphredText = encryptADFGX(alphabetMatrix, plainText, key)
            }) {
                Text(text = "Cipher")
                Modifier.fillMaxWidth(0f)
            }
            OutlinedTextField(
                value = ciphredText,
                onValueChange = { ciphredText = it },
                label = { Text("Ciphered text") }
            )
            Button(onClick = {
                plaindecipheredText += try {
                    decipherADFGX(ciphredText, key)
                } catch (e: Exception) {
                    Toast.makeText(context, "Key is invalid", Toast.LENGTH_SHORT).show()
                    ""
                }
                Log.d("dataFromMetrix", extractDataFromMatrix5x5(alphabetMatrix).toString())
                val datafromMetrix = createMatrixPairs(extractDataFromMatrix5x5(alphabetMatrix))
                Log.d("dataFromMetrixPair", datafromMetrix[0].toString())
                val plainDecipheredText = plaindecipheredText.drop(22).replace(" ", "")
                Log.d("plainDecipheredText", plainDecipheredText)
                decipheredText = replaceStringWithMatrixValues(plainDecipheredText, datafromMetrix)
                Log.d("decipheredText", decipheredText)
            }) {
                Text(text = "Decipher")
            }
            Text(text = plaindecipheredText, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = decipheredText,
                onValueChange = { decipheredText = it },
                label = { Text("Deciphered text") })
            Button(onClick = {
                alphabetMatrix = generateRandomizedAlphabetMatrix5x5()
                Log.d("generate", alphabetMatrix[0][0].toString())
            }) {
                Text(text = "Generate random matrix")
            }
            MatrixUI(matrix = alphabetMatrix,czechAlphabet)
            if (showDialog) {
                AlertDialogExample(
                    onDismissRequest = { showDialog = false },
                    onConfirmation = {
                        // Handle confirmation logic here
                        showDialog = false
                    },
                    dialogTitle = "Špatný klíč",
                    dialogText = "Klíč musí být delší než 10 znaků a musí obsahovat pouze unikátní znaky.",
                    icon = Icons.Default.Warning // Use the appropriate icon
                )
            }
        }
    }
}

@Composable
fun MatrixUI(matrix: Array<Array<Pair<Char, String>>>, czechAlphabet: Boolean) {
    val matrixSize = matrix.size

    // Display column legends (A, D, F, G, X) at the top
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.size(40.dp)) // Empty cell in the corner

        // Display legends "ADFGX"
        for (colIndex in 0 until matrixSize) {
            MatrixLegend(index = "ADFGX"[colIndex].toString(), isRow = false)
        }
    }

    // Display the matrix
    LazyColumn {
        items(matrixSize) { rowIndex ->
            val row = matrix[rowIndex]

            // Row with the row legend (A, D, F, G, X) and matrix cells
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Row legend (A, D, F, G, X)
                MatrixLegend(index = "ADFGX"[rowIndex].toString(), isRow = true)

                // Matrix cells with BasicTextField for editing
                row.forEachIndexed { colIndex, cell ->
                    val (char, _) = cell
                    MatrixCell(char = char) { newChar ->
                        // Update the character in the matrix
                        matrix[rowIndex][colIndex] = Pair(newChar, "")
                    }
                }
            }
        }
    }
}

fun extractDataFromMatrix5x5(matrix: Array<Array<Pair<Char, String>>>): String {
    val stringBuilder = StringBuilder()

    for (row in matrix) {
        for (cell in row) {
            val (char, _) = cell
            stringBuilder.append(char)
        }
    }

    return stringBuilder.toString().uppercase()
}

fun createMatrixPairs(input: String): Array<Pair<Char, String>> {
    val matrixPairs = mutableListOf<Pair<Char, String>>()
    val inputArray = input.toCharArray()
    for (n in 0 until 24){
        matrixPairs.add(Pair(inputArray[n], "ADFGX"[n/5].toString() + "ADFGX"[n%5].toString()))
    }
    return matrixPairs.toTypedArray()
}

@Composable
fun MatrixUI6x6(matrix: Array<Array<Pair<Char, String>>>) {
    val matrixSize = matrix.size

    // Display column legends (A, D, F, G, X) at the top
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.size(40.dp)) // Empty cell in the corner

        // Display legends "ADFGX"
        for (colIndex in 0 until matrixSize) {
            MatrixLegend(index = "ADFGVX"[colIndex].toString(), isRow = false)
        }
    }

    // Display the matrix
    LazyColumn {
        items(matrixSize) { rowIndex ->
            val row = matrix[rowIndex]

            // Row with the row legend (A, D, F, G, X) and matrix cells
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Row legend (A, D, F, G, X)
                MatrixLegend(index = "ADFGVX"[rowIndex].toString(), isRow = true)

                // Matrix cells with BasicTextField for editing
                row.forEachIndexed { colIndex, cell ->
                    val (char, _) = cell
                    MatrixCell(char = char) { newChar ->
                        // Update the character in the matrix
                        matrix[rowIndex][colIndex] = Pair(newChar, "")
                    }
                }
            }
        }
    }
}

@Composable
fun MatrixLegend(index: String, isRow: Boolean) {
    Surface(
        modifier = Modifier.size(40.dp),
        shape = MaterialTheme.shapes.small,
        color = Color.Gray,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = index,
                fontWeight = FontWeight.Normal,
                textAlign = if (isRow) TextAlign.End else TextAlign.Center
            )
        }
    }
}


@Composable
fun MatrixCell(char: Char, onCharChange: (Char) -> Unit) {
    var text by remember { mutableStateOf(char.toString()) }

    BasicTextField(
        value = text,
        onValueChange = {
            val alphabeticText = it.filter { it.isLetter() }
            if (czechAlphabet) text = alphabeticText.uppercase().replace("W","V") else text = alphabeticText.uppercase().replace("J","I")
            text = it.uppercase()
            val newChar = if (it.isEmpty()) ' ' else it.first()
            onCharChange(newChar)
            if(text.length> 1) text = text.drop(1)
        },
        singleLine = true,
        textStyle = androidx.compose.ui.text.TextStyle(fontWeight = FontWeight.Bold),
        modifier = Modifier.size(40.dp)
    )
}
