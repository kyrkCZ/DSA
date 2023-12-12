package com.example.dsa

import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.example.dsa.ui.theme.DSATheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.komputing.khash.keccak.Keccak
import org.komputing.khash.keccak.KeccakParameter
import java.io.File
import java.io.FileOutputStream
import java.math.BigInteger
import java.nio.file.Files
import java.security.interfaces.RSAPublicKey
import java.text.SimpleDateFormat
import java.util.Base64
import java.util.Date
import java.util.Locale
import kotlin.io.path.createTempDirectory
import com.example.dsa.RSA as RSA
import com.example.dsa.ZIP as ZIP

var files:List<File> = listOf()

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DSATheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Activity(this@MainActivity)
                }
            }
        }
    }
}

@Composable
fun Activity(context: Context) {
    var activeCipher by remember { mutableStateOf("Signiturer") }

    Column {
        NavigationBar(onCipherSelected = { cipher ->
            when (cipher) {
                "Signiturer" -> {

                }
                "Verifier" -> {

                }
            }
            activeCipher = cipher
        })

        when (activeCipher) {
            "Signiturer" -> Signiturer(context)
            "Verifier" -> Verifier(context)
        }
    }
}

@Composable
fun Verifier(context: Context) {
    var uri by remember {
        mutableStateOf(Uri.EMPTY)
    }
    var zipFileUri by remember {
        mutableStateOf(Uri.EMPTY)
    }

    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            uri = result.data?.data
            Log.d("zipFileUri", uri.toString())
            val directoryUri = createTempDirectory("UNZIP")
            val directory = directoryUri.toFile()
            val zipFile = copyFileToAppDir(context, uri, "myFile.zip")
            val zip = ZIP()
            zip.unzip(zipFile!!, directory)
            val files = directory.listFiles()
            Log.d("files", files?.size.toString())
            val publicKeyFile = File(directory, "publicKey.pub")
            val privateKeyFile = File(directory, "privateKey.pri")
            val signatureFile = File(directory, "signature.sign")
            val publicKeyBase64 = publicKeyFile.readText()
            val privateKeyBase64 = privateKeyFile.readText()
            val signatureBase64 = signatureFile.readText()
            //RSA DECODE
            val publicKey = decodeBase64ToString(publicKeyBase64)
            val privateKey = decodeBase64ToString(privateKeyBase64)
            val signature = decodeBase64ToString(signatureBase64)
            Log.d("Keys", publicKey + " " + privateKey + " " + signature)
            val publicKeyArray = publicKey.split(" ")
            val privateKeyArray = privateKey.split(" ")
            deleteFile(publicKeyFile.absolutePath)
            deleteFile(privateKeyFile.absolutePath)
            deleteFile(signatureFile.absolutePath)
            val file = loadFilesFromDirectory(directory.absolutePath)
            Log.d("file", file.toString())
            uri = file?.toUri() ?: Uri.EMPTY
        }
    }

    Button(onClick = {
        if (Environment.isExternalStorageManager()) {
            // Permission is granted. You can perform your file operations
        } else {
            // Permission is not granted. Let's ask for it
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            val uri = Uri.fromParts("package", context.packageName, null)
            intent.data = uri
            context.startActivity(intent)
        }
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            // Optionally, specify a URI for the file that should appear in the
            // system file picker when it loads.
            val pickerInitialUri = Uri.parse("content://com.android.externalstorage.documents/document/primary:Download/")
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
        }
        launcher.launch(intent)





    }, modifier = Modifier.fillMaxWidth()) {
        Text(text = "Load ZIP")
    }
    if(uri != null) FileMetadata(uri = uri, context = context)

}

@Composable
fun NavigationBar(onCipherSelected: (String) -> Unit) {
    var activeCipher by remember { mutableStateOf("Signiturer") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color.LightGray, MaterialTheme.shapes.medium),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = // Adjusted here
        Alignment.CenterVertically
    ) {
        NavigationButton("Signiturer", activeCipher == "Signiturer") {
            activeCipher = "Signiturer"
            onCipherSelected(activeCipher)
        }

        NavigationButton("Verifier", activeCipher == "Verifier") {
            activeCipher = "Verifier"
            onCipherSelected(activeCipher)
        }
    }
}

@Composable
fun NavigationButton(text: String, isActive: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.padding(8.dp),
        content = { Text(text) },
        colors = ButtonDefaults.buttonColors(
            contentColor = if (isActive) androidx.compose.ui.graphics.Color.Black else androidx.compose.ui.graphics.Color.White,
        )
    )
}

@Composable
fun FileMetadata(uri: Uri, context: Context) {
    val documentFile = DocumentFile.fromSingleUri(context, uri)
    var hash = ""
    val byteArray = loadFileAsByteArray(uri = uri, context = context)
    if (byteArray != null) {
        hash = decodeByteArrayToString(HashByteArray(byteArray))
    }
    // Get the metadata
    val name = documentFile?.name ?: "Unknown"
    val size = documentFile?.length() ?: 0
    val lastModified = documentFile?.lastModified() ?: 0

    // Convert the last modified timestamp to a readable date
    val sdf = SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.getDefault())
    val lastModifiedDate = sdf.format(Date(lastModified))

    // Display the metadata
    Text(text = "Name: $name")
    Text(text = "Size: $size bytes")
    Text(text = "Last Modified: $lastModifiedDate")
    Text(text = "URI: $uri")
    Text(text = "Path: ${documentFile?.uri?.path}")
    Text(text = "Hash: $hash")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Signiturer(context: Context){
    //RSA
    var p:BigInteger by remember { mutableStateOf(BigInteger("0")) }
    var q:BigInteger by remember { mutableStateOf(BigInteger("0")) }
    var e:BigInteger by remember { mutableStateOf(BigInteger("0")) }
    var n:BigInteger by remember { mutableStateOf(BigInteger("0")) }
    var euler:BigInteger by remember { mutableStateOf(BigInteger("0")) }
    var d:BigInteger by remember { mutableStateOf(BigInteger("0")) }

    //DSA
    var uri by remember { mutableStateOf(Uri.EMPTY) }

    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            uri = result.data?.data
            Log.d("uri", uri.toString())
        }
    }
    Button(onClick = {
        if (Environment.isExternalStorageManager()) {
            // Permission is granted. You can perform your file operations
        } else {
            // Permission is not granted. Let's ask for it
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            val uri = Uri.fromParts("package", context.packageName, null)
            intent.data = uri
            context.startActivity(intent)
        }
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            // Optionally, specify a URI for the file that should appear in the
            // system file picker when it loads.
            val pickerInitialUri = Uri.parse("content://com.android.externalstorage.documents/document/primary:Download/")
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
        }

        launcher.launch(intent)
    },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = "Load File")
    }
    if (uri != Uri.EMPTY) {
        FileMetadata(uri = uri, context = context)
        val sourceUri = uri
        val documentFile = DocumentFile.fromSingleUri(context, uri)
        val fileName = documentFile?.name ?: "Unknown"
        Log.d("fileName", fileName)

        val copiedFile = copyFileToAppDir(context, sourceUri, fileName)
        if (copiedFile != null && !files.contains(copiedFile)) {
            files = listOf(copiedFile) + files
            Log.d("files", files.toString())
        }
    }
    Button(onClick = {
        GlobalScope.launch(Dispatchers.Default) {
            p = BigInteger(RSA().generateRandomPrime().toString())
            q = BigInteger(RSA().generateRandomPrime().toString())
            n = RSA().returnN(p.toInt(), q.toInt())
            euler = RSA().eulerFunction(p.toInt(), q.toInt())
            e = RSA().chooseE(euler)
            d = RSA().returnD(e, euler)
        }
        Log.d("p", p.toString())
        Log.d("q", q.toString())
        Log.d("n", n.toString())
        Log.d("euler", euler.toString())
        Log.d("e", e.toString())
        Log.d("d", d.toString())


    },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = "Generate keys")
    }
    OutlinedTextField(
        value = "$e, $n",
        onValueChange = { },
        label = { Text("Public key:") }
    )
    OutlinedTextField(
        value = "$d, $n",
        onValueChange = { },
        label = { Text("Private key:") }
    )
    if(d!= BigInteger("0") &&n!=BigInteger("0")&&d!= BigInteger("0"))Button(onClick = {
        GlobalScope.launch(Dispatchers.Default) {
            val zip = ZIP()
            val directoryUri = createTempDirectory("DSA")
            val directory = directoryUri.toFile()
            generateFiles(
                publicKeyBase64 = encodeStringToBase64("$e $n"),
                privateKeyBase64 = encodeStringToBase64("$d $n"),
                signatureBase64 = encodeStringToBase64(
                    decodeByteArrayToString(
                        HashByteArray(
                            loadFileAsByteArray(uri = uri, context = context)!!
                        )
                    )
                )
            )
            // Create a File object for the zip file
            val zipFile = File(directory, "myFile.zip")

// Use the ZIP class to zip the directory
            ZIP().zip(zipFile, files)

// Convert the File object of the zip file to a Uri
            val zipFileUri = Uri.fromFile(zipFile)
            shareFile(context = context, file = zipFileUri.toFile())
        }
    }, modifier = Modifier.fillMaxWidth()) {
        Text(text = "Export ZIP")
    }
}

fun HashByteArray(byteArray: ByteArray): ByteArray {
    return Keccak.digest(byteArray, KeccakParameter.KECCAK_512)
}

fun loadFileAsByteArray(uri: Uri, context: Context): ByteArray? {
    return try {
        context.contentResolver.openInputStream(uri)?.readBytes()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun shareFile(context: Context, file: File) {
    val fileUri = FileProvider.getUriForFile(
        context,
        context.applicationContext.packageName + ".provider",
        file
    )
    val intent = Intent(Intent.ACTION_SEND)
    intent.type = "*/*"
    intent.putExtra(Intent.EXTRA_STREAM, fileUri)
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    context.startActivity(Intent.createChooser(intent, "Share file"))
}

fun copyFileToAppDir(context: Context, sourceUri: Uri, fileName: String): File? {
    val inputStream = context.contentResolver.openInputStream(sourceUri)
    val outputFile = File(context.filesDir, fileName)
    try {
        inputStream?.use { input ->
            FileOutputStream(outputFile).use { output ->
                input.copyTo(output)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
    return outputFile
}

fun decodeByteArrayToString(bytes: ByteArray): String {
    return bytes.joinToString("") { "%02x".format(it) }
}

fun encodeStringToBase64(input: String): String {
    return Base64.getEncoder().encodeToString(input.toByteArray())
}

fun decodeBase64ToString(input: String): String {
    return String(Base64.getDecoder().decode(input))
}

fun generateFiles(publicKeyBase64: String, privateKeyBase64: String, signatureBase64: String) {
    val directory = createTempDirectory("KEYS")
    val publicKeyFile = File(directory.toFile(), "publicKey.pub")
    publicKeyFile.writeText(publicKeyBase64)
    val privateKeyFile = File(directory.toFile(), "privateKey.pri")
    privateKeyFile.writeText(privateKeyBase64)
    val signature = File(directory.toFile(), "signature.sign")
    signature.writeText(signatureBase64)
    files = files + listOf(publicKeyFile, privateKeyFile, signature)
}

fun deleteFile(filePath: String) {
    val file = File(filePath)
    if (file.exists()) {
        val deleted = file.delete()
        if (deleted) {
            println("File deleted successfully")
        } else {
            println("Failed to delete the file")
        }
    } else {
        println("File does not exist")
    }
}

fun loadFilesFromDirectory(directoryPath: String): File? {
    val directory = File(directoryPath)
    if (directory.exists() && directory.isDirectory) {
        val files = directory.listFiles()
        if (files != null) {
            return files[0]
        } else {
            println("No files found in the directory")
        }
    } else {
        println("Directory does not exist")
    }
    return null
}

fun convertUriToFile(uri: Uri): File? {
    return uri.path?.let { File(it) }
}

@Composable
fun AlertDialogExample(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String,
    icon: ImageVector
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = dialogTitle) },
        text = { Text(text = dialogText) },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text(text = "OK")
            }
        },
        icon = {
            Icon(imageVector = icon, contentDescription = null)
        }
    )
}