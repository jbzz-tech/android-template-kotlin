package com.exemplo.app

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.util.zip.ZipFile

class MainActivity : AppCompatActivity() {

    private val extensoesCompativeis = listOf(
        ".mcpack", ".mcworld", ".mcaddon", ".mctemplate", ".mcstructure"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
                        
                        val imageView = ImageView(context).apply {
                            id = View.generateViewId()
                            layoutParams = LinearLayout.LayoutParams(96, 96)
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
                        }
                        addView(textView)
                    }
                }

                val arquivo = getItem(position)!!
                val imageView = view.getChildAt(0) as ImageView
                val textView = view.getChildAt(1) as TextView
                
                textView.text = arquivo.name
                imageView.setImageBitmap(buscarPackIcon(arquivo))
                
                return view
            }
        }
        
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val arquivo = arquivosCompativeis[position]
            abrirNoMinecraft(arquivo)
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
                    if (!entry.isDirectory && entry.name.endsWith("/pack_icon.png")) {
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

    private fun abrirNoMinecraft(arquivo: File) {
        try {
            val uri = Uri.fromFile(arquivo)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/octet-stream")
                setPackage("com.mojang.minecraftpe")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Minecraft não está instalado ou não foi possível abrir o arquivo", Toast.LENGTH_LONG).show()
        }
    }
}