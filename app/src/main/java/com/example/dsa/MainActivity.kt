package com.example.dsa

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import java.security.PublicKey
import java.text.SimpleDateFormat
import java.util.Base64
import java.util.Date
import java.util.Locale
import kotlin.io.path.createTempDirectory
import com.example.dsa.RSA as RSA
import com.example.dsa.ZIP as ZIP
import com.example.dsa.RSA.Key
import com.example.dsa.RSA.KeyPair
import kotlinx.coroutines.DelicateCoroutinesApi

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
@Preview(showBackground = true)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyNavigationDrawer() {

    val navigationState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedItemIndex by rememberSaveable {
        mutableStateOf(0)
    }

    val items = listOf(
        DrawerItem(
            title = "Home",
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home,
        ),
        DrawerItem(
            title = "Notification",
            selectedIcon = Icons.Filled.Info,
            unselectedIcon = Icons.Outlined.Info,
            badgeCount = 45
        ),
        DrawerItem(
            title = "Favorites",
            selectedIcon = Icons.Filled.Favorite,
            unselectedIcon = Icons.Outlined.FavoriteBorder,
        ),
    )

    // to define navigation drawer here
}

data class DrawerItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badgeCount: Int? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Activity(context: Context) {
    var activeCipher by remember { mutableStateOf("Signiturer") }

        Column {

            Row {
                NavigationBar(onCipherSelected = { cipher ->
                    when (cipher) {
                        "Signiturer" -> {

                        }

                        "Verifier" -> {

                        }
                    }
                    activeCipher = cipher
                })
            }

            when (activeCipher) {
                "Signiturer" -> Signiturer(context)
                "Verifier" -> Verifier(context)
            }
        }
    }

@OptIn(DelicateCoroutinesApi::class)
@Composable
fun Verifier(context: Context) {
    var uri by remember {
        mutableStateOf(Uri.EMPTY)
    }
    var zipFileUri by remember {
        mutableStateOf(Uri.EMPTY)
    }

    var showDialogNotEqual by remember { mutableStateOf(false) }
    var showDialogEqual by remember { mutableStateOf(false) }

    var KeyPair: RSA.KeyPair? = null
    var publicKey: RSA.Key? by remember { mutableStateOf(null) }
    var privateKey: RSA.Key? by remember { mutableStateOf(null) }

    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            GlobalScope.launch(Dispatchers.Default) {
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
            Log.d("publicKeyBase64", publicKeyBase64)
            val privateKeyBase64 = privateKeyFile.readText()
            Log.d("privateKeyBase64", privateKeyBase64)
            val signatureBase64 = signatureFile.readText()
                Log.d("signatureBase64", signatureBase64)

            val publicKey = decodeBase64ToString(publicKeyBase64)
                Log.d("DecodedpublicKey", publicKey)
            val privateKey = decodeBase64ToString(privateKeyBase64)
                Log.d("DecodedprivateKey", privateKey)
            val signatureRSA = decodeBase64ToString(signatureBase64)
                Log.d("Decodedsignature", signatureRSA)
            val RSA = RSA()
                val keyPair = KeyPair(
                    publicKey = Key(
                        modulus = BigInteger(publicKey.substringAfter("modulus=").substringBefore(", exponent=")),
                        exponent = BigInteger(publicKey.substringAfter("exponent=").substringBefore(")"))
                    ),
                    privateKey = Key(
                        modulus = BigInteger(privateKey.substringAfter("modulus=").substringBefore(", exponent=")),
                        exponent = BigInteger(privateKey.substringAfter("exponent=").substringBefore(")"))
                    )
                )
                Log.d("keyPair", keyPair.toString())
            val signature = RSA.decrypt(signatureRSA, keyPair.publicKey)
                Log.d("signature", signature)
            deleteFile(publicKeyFile.absolutePath)
            deleteFile(privateKeyFile.absolutePath)
            deleteFile(signatureFile.absolutePath)
            val file = loadFilesFromDirectory(directory.absolutePath)
            files[0] = file!!
            Log.d("file", file.toString())
            uri = file?.toUri() ?: Uri.EMPTY
            HashByteArray(
                loadFileAsByteArray(uri = uri, context = context)!!
            )
            val hash = decodeByteArrayToString(
                HashByteArray(
                    loadFileAsByteArray(uri = uri, context = context)!!
                )
            )
            if(!compareStrings(hash, signature)){
                showDialogEqual = true
            }
            else{
                showDialogNotEqual = true
            }
            }
        }
    }

    Button(onClick = {
        GlobalScope.launch {
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
                val pickerInitialUri =
                    Uri.parse("content://com.android.externalstorage.documents/document/primary:Download/")
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
            }
            launcher.launch(intent)
        }




    }, modifier = Modifier.fillMaxWidth()) {
        Text(text = "Load ZIP")
    }
    if(uri != Uri.EMPTY) FileMetadata(uri = uri, context = context)
    if(uri != Uri.EMPTY) Button(
        onClick = {
            GlobalScope.launch {
                val fileUri = FileProvider.getUriForFile(
                    context,
                    context.applicationContext.packageName + ".provider",
                    files[0]
                )
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "*/*"
                intent.putExtra(Intent.EXTRA_STREAM, fileUri)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                context.startActivity(Intent.createChooser(intent, "Share file"))
            }
    }, modifier = Modifier.fillMaxWidth()) {
        Text(text = "Open file")
    }
    if(showDialogEqual){
        AlertDialogExample(
            onDismissRequest = { showDialogEqual = false },
            onConfirmation = { showDialogEqual = false },
            dialogTitle = "Signature is equal",
            dialogText = "Files are equal, the file is safe to open",
            icon = ImageVector.vectorResource(id = R.drawable.done_fill0_wght400_grad0_opsz24)
        )

    }
    if(showDialogNotEqual){
        AlertDialogExample(
            onDismissRequest = { showDialogNotEqual = false },
            onConfirmation = { showDialogNotEqual = false },
            dialogTitle = "Signature is not equal",
            dialogText = "Files are not equal, the file is not safe to open, continue with cation",
            icon = ImageVector.vectorResource(id = R.drawable.close_fill0_wght400_grad0_opsz24)
        )
    }

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
    var KeyPair by remember { mutableStateOf<RSA.KeyPair?>(null) }
    var PublicKey by remember { mutableStateOf(BigInteger("0")) }
    var PrivateKey by remember { mutableStateOf(BigInteger("0")) }
    var uri by remember { mutableStateOf(Uri.EMPTY) }

    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            uri = result.data?.data
            Log.d("uri", uri.toString())
        }
    }
    Button(onClick = {
        if (Environment.isExternalStorageManager()) {
        } else {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            val uri = Uri.fromParts("package", context.packageName, null)
            intent.data = uri
            context.startActivity(intent)
        }
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
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
            //val RSA = RSA()
            //val p = RSA.generateRandomPrime()
            //val q = RSA.generateRandomPrime()
            //val RSAEncryptor = RSAEncryptor(p,q, RSA.chooseE(RSA.eulerFunction(p = p.toBigInteger(), q = q.toBigInteger())).toInt())
            val RSA = RSA()
            val keyPair = RSA.generateKeyPair(512)
            KeyPair = keyPair
            PublicKey = keyPair.publicKey.exponent + keyPair.publicKey.modulus
            PrivateKey = keyPair.privateKey.exponent + keyPair.privateKey.modulus
        }
    },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = "Generate keys")
    }
    OutlinedTextField(
        value = "$PublicKey",
        onValueChange = { },
        label = { Text("Public key:") },
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = "$PrivateKey",
        onValueChange = { },
        label = { Text("Private key:")},
        modifier = Modifier.fillMaxWidth()
    )
    if(PublicKey!=BigInteger("0")&& PrivateKey!= BigInteger("0"))Button(onClick = {
        GlobalScope.launch(Dispatchers.Default) {
            val RSA = RSA()
            val zip = ZIP()
            val directoryUri = createTempDirectory("DSA")
            val directory = directoryUri.toFile()
            val Signature = (decodeByteArrayToString(
                HashByteArray(
                    loadFileAsByteArray(
                        uri = uri,
                        context = context
                    )!!
                )
            ))
            Log.d("Signature", Signature)
            val PublicKey = KeyPair
            Log.d("PublicKey", PublicKey.toString())
            val RSASignarure = RSA.encrypt(Signature, PublicKey!!.privateKey)
            Log.d("RSASignarure", RSASignarure)
            Log.d("PublicKey", PublicKey.toString())
            Log.d("PrivateKey", PrivateKey.toString())
            generateFiles(
                publicKeyBase64 = encodeStringToBase64("${PublicKey.publicKey}"),
                privateKeyBase64 = encodeStringToBase64("${PublicKey.privateKey}"),
                signatureBase64 = encodeStringToBase64("$RSASignarure")
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

fun compareStrings(string1:String, string2:String):Boolean
{
    return string1 == string2
}

