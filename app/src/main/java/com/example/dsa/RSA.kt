package com.example.dsa

import android.util.Log
import java.math.BigInteger
import java.security.SecureRandom

class RSA {
    fun returnN(p:Int,q:Int): BigInteger {
        val p = BigInteger(p.toString())
        val q = BigInteger(q.toString())
        val n = p.multiply(q)
        return n
    }

    fun eulerFunction(p:Int,q: Int): BigInteger {
        val p = BigInteger(p.toString())
        val q = BigInteger(q.toString())
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

    fun cipher(plainText: String, e: BigInteger, n: BigInteger): String {
        var cipheredText = ""

        for (char in plainText) {
            val asciiValue = BigInteger.valueOf(char.toLong())
            val cipheredChar = asciiValue.modPow(e, n)
            cipheredText += cipheredChar.toString(36) + " " // Convert to base 36 to shorten the string
        }

        return cipheredText.trim() // Remove trailing space
    }

    fun decipher(cipherText: String, d: BigInteger, n: BigInteger): String {
        var decipheredText = ""

        val cipheredChars = cipherText.split(" ")
        for (cipheredChar in cipheredChars) {
            val cipheredValue = BigInteger(cipheredChar, 36) // Convert back from base 36
            val decipheredChar = cipheredValue.modPow(d, n).toChar()
            decipheredText += decipheredChar
        }

        return decipheredText
    }
}