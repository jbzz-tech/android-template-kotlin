package com.exemplo.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
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
import java.io.File
import java.util.zip.ZipFile

class MainActivity : AppCompatActivity() {

    private val extensoesCompativeis = listOf(
        ".mcpack", ".mcworld", ".mcaddon", ".mctemplate", ".mcstructure"
    )

    private val PERMISSAO_STORAGE = 1001
    private val cacheIcons = mutableMapOf<String, android.graphics.Bitmap?>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnDownload = findViewById<Button>(R.id.btnDownload)
        btnDownload.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.curseforge.com/minecraft/search?class=mc-addons"))
            startActivity(intent)
        }

        val btnMenu = findViewById<Button>(R.id.btnMenu)
        btnMenu.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://chat.whatsapp.com/SEU_LINK_DE_CONVITE_AQUI"))
            startActivity(intent)
        }

        verificarPermissao()
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

    private fun carregarArquivos() {
        val listView = findViewById<ListView>(R.id.listViewArquivos)
        val arquivosCompativeis = buscarArquivosCompativeis()
        
        if (arquivosCompativeis.isEmpty()) {
            Toast.makeText(this, "Nenhum arquivo compatível encontrado", Toast.LENGTH_LONG).show()
            return
        }

        // Carrega todos os ícones uma vez e armazena no cache
        for (arquivo in arquivosCompativeis) {
            if (!cacheIcons.containsKey(arquivo.absolutePath)) {
                cacheIcons[arquivo.absolutePath] = buscarPackIcon(arquivo)
            }
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
                val icon = cacheIcons[arquivo.absolutePath]
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
            val zipFile = ZipFile(arquivo)
            val entries = zipFile.entries()
            val extensoesImagem = listOf(".png", ".jpg", ".jpeg")
            val subpastas = mutableListOf<String>()

            // Primeiro: procurar na raiz
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                val entryName = entry.name

                if (!entry.isDirectory) {
                    // Verifica se é uma imagem na raiz (sem barra no nome)
                    if (!entryName.contains("/")) {
                        if (extensoesImagem.any { entryName.lowercase().endsWith(it) }) {
                            val inputStream = zipFile.getInputStream(entry)
                            val bitmap = BitmapFactory.decodeStream(inputStream)
                            inputStream.close()
                            zipFile.close()
                            return bitmap
                        }
                    } else {
                        // Coleta nomes de subpastas (primeiro nível)
                        val parts = entryName.split("/")
                        if (parts.size >= 2) {
                            val subpasta = parts[0]
                            if (!subpastas.contains(subpasta)) {
                                subpastas.add(subpasta)
                            }
                        }
                    }
                } else {
                    // Coleta nomes de subpastas (diretórios)
                    val entryName = entry.name.trimEnd('/')
                    if (!entryName.contains("/")) {
                        if (!subpastas.contains(entryName)) {
                            subpastas.add(entryName)
                        }
                    }
                }
            }

            // Segundo: procurar nas subpastas (apenas primeiro nível)
            for (subpasta in subpastas) {
                val entries2 = zipFile.entries()
                while (entries2.hasMoreElements()) {
                    val entry = entries2.nextElement()
                    val entryName = entry.name

                    if (!entry.isDirectory && entryName.startsWith("$subpasta/")) {
                        // Verifica se está exatamente um nível abaixo da subpasta
                        val nomeAposSubpasta = entryName.removePrefix("$subpasta/")
                        if (!nomeAposSubpasta.contains("/")) {
                            if (extensoesImagem.any { nomeAposSubpasta.lowercase().endsWith(it) }) {
                                val inputStream = zipFile.getInputStream(entry)
                                val bitmap = BitmapFactory.decodeStream(inputStream)
                                inputStream.close()
                                zipFile.close()
                                return bitmap
                            }
                        }
                    }
                }
            }

            zipFile.close()
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
