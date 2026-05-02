package com.exemplo.app

import android.content.Intent
import android.graphics.BitmapFactory
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
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import java.util.zip.ZipFile

class MainActivity : AppCompatActivity() {

    private val extensoesCompativeis = listOf(
        ".mcpack", ".mcworld", ".mcaddon", ".mctemplate", ".mcstructure"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnMenu = findViewById<Button>(R.id.btnMenu)
        btnMenu.setOnClickListener { view ->
            val popup = PopupMenu(this, view)
            popup.menu.add("CurseForge")
            popup.menu.add("MCPEDL")
            popup.setOnMenuItemClickListener { item ->
                when (item.title) {
                    "CurseForge" -> {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.curseforge.com/minecraft/search?class=mc-addons"))
                        startActivity(intent)
                        true
                    }
                    "MCPEDL" -> {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://mcpedl.com"))
                        startActivity(intent)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

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
            carregarArquivos()
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
