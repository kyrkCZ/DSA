package com.example.dsa

import android.util.Log
import androidx.compose.runtime.MutableState
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.PublicKey
import java.security.SecureRandom
import java.security.spec.InvalidKeySpecException
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import javax.crypto.Cipher

class RSA() {


    fun returnN(p:Int,q:Int): BigInteger {
        val p = BigInteger(p.toString())
        val q = BigInteger(q.toString())
        val n = p.multiply(q)
        return n
    }

    fun eulerFunction(p:BigInteger,q: BigInteger): BigInteger {
        val eulerFunction = p.subtract(BigInteger("1"))*q.subtract(BigInteger("1"))
        return eulerFunction
    }

    fun chooseE(phi: BigInteger): BigInteger {
        val random = SecureRandom()
        val minExponent = BigInteger.valueOf(2)
        val maxExponent = phi.subtract(BigInteger.ONE) // 'e' should be less than φ(n)

        var potentialExponent: BigInteger
        do {
            potentialExponent = BigInteger(maxExponent.bitLength(), random)
        } while (potentialExponent < minExponent || potentialExponent >= maxExponent || potentialExponent.gcd(phi) != BigInteger.ONE)

        return potentialExponent
    }

    fun calculateGCD(a: BigInteger, b: BigInteger): BigInteger {
        return a.gcd(b)
    }


    fun returnD(e: BigInteger, phi: BigInteger): BigInteger {
        Log.d("returnD", e.modInverse(phi).toString())
        return e.modInverse(phi)
    }

    fun generateRandomPrime(): Int {
        val start = 100000000
        val end = 1000000000
        val random = java.util.Random()
        for (i in 0..100) {
            val number = start + random.nextInt(end - start + 1)
            if (isPrime(number)) {
                return number
            }
        }
        return 0
    }

    fun isPrime(number: Int): Boolean {
        if (number <= 1) {
            return false
        }
        for (i in 2..number / 2) {
            if (number % i == 0) {
                return false
            }
        }
        return true
    }

    data class KeyPair(val publicKey: Key, val privateKey: Key)

    data class Key(val modulus: BigInteger, val exponent: BigInteger)

    fun generateKeyPair(bitLength: Int): KeyPair {
        val random = SecureRandom()
        val p = generateRandomPrime().toBigInteger()
        val q = generateRandomPrime().toBigInteger()
        val modulus = p.multiply(q)
        val phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE))
        val publicKeyExponent = BigInteger("65537")
        val privateKeyExponent = publicKeyExponent.modInverse(phi)
        val publicKey = Key(modulus, publicKeyExponent)
        val privateKey = Key(modulus, privateKeyExponent)
        return KeyPair(publicKey, privateKey)
    }

    fun encrypt(message: String, publicKey: Key): String {
        val messageBytes = message.toByteArray(StandardCharsets.UTF_8)
        val messageInt = BigInteger(messageBytes)
        Log.d("MessageInt", messageInt.toString())
        val encryptedInt = messageInt.modPow(publicKey.exponent, publicKey.modulus)
        val encryptedBytes = encryptedInt.toByteArray()
        return encryptedBytes.toString()
    }

    fun decrypt(encrypted: String, privateKey: Key): String {
        val encryptedBytes = encrypted.toByteArray(StandardCharsets.UTF_8)
        val encryptedInt = BigInteger(encryptedBytes)
        Log.d("EncryptedInt", encryptedInt.toString())
        val decryptedInt = encryptedInt.modPow(privateKey.exponent, privateKey.modulus)
        val decryptedBytes = decryptedInt.toByteArray()
        return String(decryptedBytes, StandardCharsets.UTF_8)
    }
}