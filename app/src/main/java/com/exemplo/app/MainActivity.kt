package com.exemplo.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File

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

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, arquivosCompativeis.map { it.name })
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val arquivo = arquivosCompativeis[position]
            abrirNoMinecraft(arquivo)
        }
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