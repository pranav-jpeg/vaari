package com.vaari.app.ui.home

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.vaari.app.R
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

class CameraFragment : Fragment() {

    private lateinit var ivPreview: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvResult: TextView
    private lateinit var btnPickImage: Button
    private lateinit var btnBack: Button

    private val PAT = "00828d5455ad42e0a20ecc74564464f5"
    private val USER_ID = "clarifai"
    private val APP_ID = "main"
    private val MODEL_ID = "general-image-recognition"
    private val MODEL_VERSION_ID = "aa7f35c01e0642fda5cf400f543e7c40"

    private val cropKeywords = setOf(
        "cherry", "coffee", "cucumber", "makhana", "lemon", "olive", "millet", "bajra",
        "tobacco", "almond", "banana", "cardamom", "chilli", "clove", "coconut", "cotton",
        "gram", "jowar", "jute", "maize", "corn", "mustard", "papaya", "pineapple", "rice",
        "soybean", "soya", "sugarcane", "sunflower", "tea", "tomato", "mung", "wheat",
        "plant", "crop", "grain", "seed", "vegetable", "fruit", "leaf", "agriculture", "farm"
    )

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { analyzeImage(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ivPreview    = view.findViewById(R.id.ivPreview)
        progressBar  = view.findViewById(R.id.progressBar)
        tvResult     = view.findViewById(R.id.tvResult)
        btnPickImage = view.findViewById(R.id.btnPickImage)
        btnBack      = view.findViewById(R.id.btnBack)

        btnPickImage.setOnClickListener { pickImageLauncher.launch("image/*") }
        btnBack.setOnClickListener { findNavController().navigateUp() }
    }

    private fun analyzeImage(uri: Uri) {
        ivPreview.visibility = View.VISIBLE
        progressBar.visibility = View.VISIBLE
        tvResult.visibility = View.GONE
        tvResult.text = ""

        val bitmap = uriToBitmap(uri) ?: run {
            Toast.makeText(requireContext(), getString(R.string.error_load_image), Toast.LENGTH_SHORT).show()
            progressBar.visibility = View.GONE
            return
        }

        ivPreview.setImageBitmap(bitmap)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val base64Image = bitmapToBase64(bitmap)
                val result = callClarifaiAPI(base64Image)
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    tvResult.visibility = View.VISIBLE
                    tvResult.text = result
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    tvResult.visibility = View.VISIBLE
                    tvResult.text = getString(R.string.error_format, e.message)
                }
            }
        }
    }

    private fun uriToBitmap(uri: Uri): Bitmap? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) { null }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        val scaled = if (bitmap.width > 512 || bitmap.height > 512) {
            val ratio = minOf(512f / bitmap.width, 512f / bitmap.height)
            Bitmap.createScaledBitmap(bitmap, (bitmap.width * ratio).toInt(), (bitmap.height * ratio).toInt(), true)
        } else bitmap
        scaled.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    private fun callClarifaiAPI(base64Image: String): String {
        val url = URL("https://api.clarifai.com/v2/users/$USER_ID/apps/$APP_ID/models/$MODEL_ID/versions/$MODEL_VERSION_ID/outputs")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Authorization", "Key $PAT")
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true
        connection.connectTimeout = 15000
        connection.readTimeout = 15000

        val requestBody = """
{
  "inputs": [{"data": {"image": {"base64": "$base64Image"}}}]
}""".trimIndent()

        connection.outputStream.use { it.write(requestBody.toByteArray()) }

        val responseCode = connection.responseCode
        val response = if (responseCode == 200)
            connection.inputStream.bufferedReader().readText()
        else
            connection.errorStream?.bufferedReader()?.readText() ?: "Unknown error"

        return parseResponse(response)
    }

    private fun parseResponse(jsonResponse: String): String {
        return try {
            val json = JSONObject(jsonResponse)
            val status = json.getJSONObject("status")
            if (status.getInt("code") != 10000) {
                return getString(R.string.api_error_format, status.getString("description"))
            }

            val outputs  = json.getJSONArray("outputs")
            val concepts = outputs.getJSONObject(0).getJSONObject("data").getJSONArray("concepts")

            val cropMatches = mutableListOf<Pair<String, Double>>()
            val allLabels   = mutableListOf<Pair<String, Double>>()

            for (i in 0 until concepts.length()) {
                val concept = concepts.getJSONObject(i)
                val name  = concept.getString("name").lowercase()
                val value = concept.getDouble("value")
                allLabels.add(Pair(concept.getString("name"), value))
                if (cropKeywords.any { keyword -> name.contains(keyword) }) {
                    cropMatches.add(Pair(concept.getString("name"), value))
                }
            }

            if (cropMatches.isNotEmpty()) {
                val top = cropMatches.maxByOrNull { it.second }!!
                val confidence = (top.second * 100).toInt()
                getString(R.string.detected_format, top.first, confidence)
            } else {
                val top3 = allLabels.take(3).joinToString(", ") {
                    "${it.first} (${(it.second * 100).toInt()}%)"
                }
                getString(R.string.top_labels_format, top3)
            }
        } catch (e: Exception) {
            getString(R.string.parse_error_format, e.message)
        }
    }
}