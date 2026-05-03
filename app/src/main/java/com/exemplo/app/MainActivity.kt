package com.exemplo.app

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.File
import java.net.URL
import java.util.zip.ZipFile

class MainActivity : AppCompatActivity() {

    private val extensoesCompativeis = listOf(
        ".mcpack", ".mcworld", ".mcaddon", ".mctemplate", ".mcstructure"
    )

    private val PERMISSAO_STORAGE = 1001
    private val VERSAO_APP = "1.0"
    private val URL_VERSAO = "https://raw.githubusercontent.com/kry-tech/android-template-kotlin/main/versao.json"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnDownload = findViewById<Button>(R.id.btnDownload)
        btnDownload.setOnClickListener {
            mostrarDialogDownload()
        }

        verificarPermissao()
        verificarAtualizacao()
    }

    private fun verificarAtualizacao() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val json = URL(URL_VERSAO).readText()
                val jsonObject = JSONObject(json)
                val versaoNova = jsonObject.getString("versao")
                val linkDownload = jsonObject.getString("link")
                val changelog = jsonObject.optString("changelog", "")

                if (versaoNova != VERSAO_APP) {
                    withContext(Dispatchers.Main) {
                        mostrarDialogAtualizacao(versaoNova, linkDownload, changelog)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun mostrarDialogAtualizacao(versaoNova: String, link: String, changelog: String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_atualizacao)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val txtVersao = dialog.findViewById<TextView>(R.id.txtVersaoNova)
        val txtChangelog = dialog.findViewById<TextView>(R.id.txtChangelog)
        val btnAtualizar = dialog.findViewById<Button>(R.id.btnAtualizar)
        val btnDepois = dialog.findViewById<Button>(R.id.btnDepois)

        txtVersao.text = "Versão $versaoNova disponível!"
        if (changelog.isNotEmpty()) {
            txtChangelog.text = changelog
        } else {
            txtChangelog.visibility = View.GONE
        }

        btnAtualizar.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
            startActivity(intent)
            dialog.dismiss()
        }

        btnDepois.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun verificarPermissao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Toast.makeText(this, "Permita acesso a todos os arquivos nas configurações", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:${packageName}")
                }
                startActivity(intent)
            } else {
                carregarArquivos()
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSAO_STORAGE
                )
            } else {
                carregarArquivos()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSAO_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                carregarArquivos()
            } else {
                Toast.makeText(this, "Permissão negada. O app não pode acessar os arquivos.", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                carregarArquivos()
            }
        }
    }

    private fun mostrarDialogDownload() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_download)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val btnCurseForge = dialog.findViewById<Button>(R.id.btnCurseForge)
        val btnMcpedl = dialog.findViewById<Button>(R.id.btnMcpedl)
        val btnMinecraftDownload = dialog.findViewById<Button>(R.id.btnMinecraftDownload)

        btnCurseForge.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.curseforge.com/minecraft/search?class=mc-addons"))
            startActivity(intent)
            dialog.dismiss()
        }

        btnMcpedl.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://mcpedl.com"))
            startActivity(intent)
            dialog.dismiss()
        }

        btnMinecraftDownload.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://mcpedl.org/download-minecraft"))
            startActivity(intent)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun carregarArquivos() {
        val listView = findViewById<ListView>(R.id.listViewArquivos)
        val arquivosCompativeis = buscarArquivosCompativeis()
        
        if (arquivosCompativeis.isEmpty()) {
            Toast.makeText(this, "Nenhum arquivo compatível encontrado", Toast.LENGTH_LONG).show()
            return
        }

        val adapter = object : ArrayAdapter<File>(this, 0, arquivosCompativeis) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view: LinearLayout
                
                if (convertView != null) {
                    view = convertView as LinearLayout
                } else {
                    view = LinearLayout(context).apply {
                        orientation = LinearLayout.HORIZONTAL
                        setPadding(16, 16, 16, 16)
                        gravity = Gravity.CENTER_VERTICAL
                        
                        val imageView = ImageView(context).apply {
                            id = View.generateViewId()
                            layoutParams = LinearLayout.LayoutParams(96, 96)
                            scaleType = ImageView.ScaleType.CENTER_CROP
                            setBackgroundColor(0xFF2b1709.toInt())
                        }
                        addView(imageView)
                        
                        val textView = TextView(context).apply {
                            id = View.generateViewId()
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                setMargins(16, 0, 0, 0)
                            }
                            textSize = 16f
                            setTextColor(0xFFf0e6c5.toInt())
                            typeface = android.graphics.Typeface.MONOSPACE
                            gravity = Gravity.CENTER_VERTICAL
                        }
                        addView(textView)
                    }
                }

                val arquivo = getItem(position)!!
                val imageView = view.getChildAt(0) as ImageView
                val textView = view.getChildAt(1) as TextView
                
                textView.text = arquivo.name
                val icon = buscarPackIcon(arquivo)
                if (icon != null) {
                    imageView.setImageBitmap(icon)
                } else {
                    imageView.setBackgroundColor(0xFF5a3a1a.toInt())
                }
                
                return view
            }
        }
        
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val arquivo = arquivosCompativeis[position]
            abrirArquivo(arquivo)
        }
    }

    private fun buscarPackIcon(arquivo: File): android.graphics.Bitmap? {
        try {
            val extensao = arquivo.name.lowercase()
            if (extensao.endsWith(".mcpack") || extensao.endsWith(".mcaddon") || extensao.endsWith(".mctemplate")) {
                val zipFile = ZipFile(arquivo)
                val entries = zipFile.entries()
                
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    val entryName = entry.name
                    if (!entry.isDirectory && (entryName == "pack_icon.png" || entryName.endsWith("/pack_icon.png"))) {
                        val inputStream = zipFile.getInputStream(entry)
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        inputStream.close()
                        zipFile.close()
                        return bitmap
                    }
                }
                zipFile.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun buscarArquivosCompativeis(): List<File> {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val arquivos = mutableListOf<File>()

        if (downloadsDir.exists()) {
            downloadsDir.listFiles()?.forEach { file ->
                if (extensoesCompativeis.any { file.name.lowercase().endsWith(it) }) {
                    arquivos.add(file)
                }
            }
        }

        return arquivos
    }

    private fun abrirArquivo(arquivo: File) {
        try {
            val uri = FileProvider.getUriForFile(this, "${packageName}.provider", arquivo)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/octet-stream")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                setPackage("com.mojang.minecraftpe")
            }
            startActivity(intent)
        } catch (e: Exception) {
            try {
                val uri = FileProvider.getUriForFile(this, "${packageName}.provider", arquivo)
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/octet-stream")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(intent)
            } catch (e2: Exception) {
                Toast.makeText(this, "Minecraft não está instalado", Toast.LENGTH_LONG).show()
            }
        }
    }
}