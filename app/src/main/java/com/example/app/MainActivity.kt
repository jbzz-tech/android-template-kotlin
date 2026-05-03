package com.exemplo.app

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import java.security.MessageDigest
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.mail.*
import javax.mail.internet.*

class MainActivity : AppCompatActivity() {

    private var saldo = 0.0
    private var anunciosAssistidos = 0
    private val limiteAnuncios = 100
    private val valorPorAnuncio = 0.01
    private val limiteDiario = 1.0
    private val valorMinimoSaque = 1.0
    
    private var codigoEmail = ""
    private var codigoSMS = ""
    private var timestampCodigo = 0L
    private val expiracaoCodigo = 5 * 60 * 1000L
    private var tentativasSaque = 0
    private val maxTentativasSaque = 3
    private var dispositivoBloqueado = false
    
    // LOGS DE ATIVIDADE
    private val logAtividade = StringBuilder()
    private var ultimoAnuncioTimestamp = 0L
    private var inicioSessao = 0L
    
    private var toquesSaldo = 0

    private lateinit var tvSaldo: TextView
    private lateinit var tvAnuncios: TextView
    private lateinit var tvRedeAtual: TextView
    private lateinit var btnAssistirAnuncio: Button
    private lateinit var btnSacar: Button
    private lateinit var adManager: AdManager
    private lateinit var sharedPrefs: android.content.SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvSaldo = findViewById(R.id.tvSaldo)
        tvAnuncios = findViewById(R.id.tvAnuncios)
        tvRedeAtual = findViewById(R.id.tvRedeAtual)
        btnAssistirAnuncio = findViewById(R.id.btnAssistirAnuncio)
        btnSacar = findViewById(R.id.btnSacar)

        sharedPrefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        adManager = AdManager(this)
        adManager.initializeAllNetworks()

        inicioSessao = System.currentTimeMillis()
        verificarDispositivoBloqueado()
        carregarDadosSalvos()
        registrarLog("APP ABERTO - Sessão iniciada")
        atualizarInterface()

        btnAssistirAnuncio.setOnClickListener {
            assistirAnuncio()
        }

        btnSacar.setOnClickListener {
            solicitarChavePix()
        }
        
        // Botão secreto para painel admin (5 toques no saldo)
        tvSaldo.setOnClickListener {
            toquesSaldo++
            if (toquesSaldo >= 5) {
                toquesSaldo = 0
                mostrarPainelAdmin()
            }
        }
    }

    private fun registrarLog(acao: String) {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val timestamp = sdf.format(Date())
        logAtividade.append("[$timestamp] $acao\n")
    }

    private fun verificarDispositivoBloqueado() {
        dispositivoBloqueado = sharedPrefs.getBoolean("dispositivo_bloqueado", false)
        if (dispositivoBloqueado) {
            btnSacar.isEnabled = false
            btnSacar.text = "Dispositivo bloqueado"
        }
    }

    private fun carregarDadosSalvos() {
        saldo = sharedPrefs.getFloat("saldo", 0.0f).toDouble()
        anunciosAssistidos = sharedPrefs.getInt("anuncios_assistidos", 0)
        tentativasSaque = sharedPrefs.getInt("tentativas_saque", 0)
    }

    private fun salvarDados() {
        sharedPrefs.edit().apply {
            putFloat("saldo", saldo.toFloat())
            putInt("anuncios_assistidos", anunciosAssistidos)
            putInt("tentativas_saque", tentativasSaque)
            putBoolean("dispositivo_bloqueado", dispositivoBloqueado)
            apply()
        }
    }

    private fun getDeviceId(): String {
        return Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
    }

    private fun gerarHash(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun gerarCodigoAleatorio(tamanho: Int = 8): String {
        val caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%"
        return (1..tamanho).map { caracteres.random() }.joinToString("")
    }

    private fun assistirAnuncio() {
        if (dispositivoBloqueado) {
            Toast.makeText(this, "Dispositivo bloqueado por segurança", Toast.LENGTH_SHORT).show()
            return
        }

        if (anunciosAssistidos >= limiteAnuncios) {
            Toast.makeText(this, "Limite diário de anúncios atingido!", Toast.LENGTH_SHORT).show()
            return
        }

        if (saldo >= limiteDiario) {
            Toast.makeText(this, "Limite diário de R$ 1,00 atingido!", Toast.LENGTH_SHORT).show()
            return
        }

        val network = adManager.getNextAdNetwork()
        tvRedeAtual.text = "Rede atual: $network"
        
        adManager.showRewardedAd {
            anunciosAssistidos++
            saldo += valorPorAnuncio
            ultimoAnuncioTimestamp = System.currentTimeMillis()

            if (saldo > limiteDiario) {
                saldo = limiteDiario
            }

            runOnUiThread {
                registrarLog("ANÚNCIO #$anunciosAssistidos - Rede: $network - Saldo: R$ ${"%.2f".format(saldo)}")
                salvarDados()
                atualizarInterface()
                Toast.makeText(this, "+R$ 0,01 ($network)", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun solicitarChavePix() {
        if (dispositivoBloqueado) {
            Toast.makeText(this, "Dispositivo bloqueado por segurança", Toast.LENGTH_SHORT).show()
            return
        }

        if (tentativasSaque >= maxTentativasSaque) {
            dispositivoBloqueado = true
            salvarDados()
            registrarLog("DISPOSITIVO BLOQUEADO - Muitas tentativas de saque")
            Toast.makeText(this, "Muitas tentativas. Dispositivo bloqueado!", Toast.LENGTH_LONG).show()
            return
        }

        if (saldo < valorMinimoSaque) {
            Toast.makeText(this, "Saldo mínimo para saque é R$ 1,00", Toast.LENGTH_SHORT).show()
            return
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Verificação de Segurança")

        val input = EditText(this)
        input.hint = "Digite sua chave PIX"
        builder.setView(input)

        builder.setPositiveButton("Solicitar Códigos") { dialog, _ ->
            val chavePix = input.text.toString().trim()
            
            if (chavePix.isEmpty()) {
                Toast.makeText(this, "Digite uma chave PIX válida!", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            val formatador = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
            val valorFormatado = formatador.format(saldo)
            val deviceId = getDeviceId()
            
            codigoEmail = gerarCodigoAleatorio(12)
            codigoSMS = gerarCodigoAleatorio(8)
            timestampCodigo = System.currentTimeMillis()
            
            val hashCodigo = gerarHash("$codigoEmail:$codigoSMS:$deviceId:$timestampCodigo")
            
            tentativasSaque++
            salvarDados()
            registrarLog("SAQUE SOLICITADO - Chave: ${chavePix.take(5)}... - Tentativa: $tentativasSaque")
            
            enviarCodigosPorEmail(chavePix, valorFormatado, deviceId, hashCodigo)
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun enviarCodigosPorEmail(chavePix: String, valor: String, deviceId: String, hash: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val props = Properties().apply {
                    put("mail.smtp.auth", "true")
                    put("mail.smtp.starttls.enable", "true")
                    put("mail.smtp.host", "smtp.gmail.com")
                    put("mail.smtp.port", "587")
                    put("mail.smtp.ssl.protocols", "TLSv1.2")
                }

                val session = Session.getInstance(props, object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication("SEU_EMAIL@gmail.com", "SUA_SENHA_APP_16_DIGITOS")
                    }
                })

                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                
                // Calcular tempo de atividade
                val tempoAtividade = (System.currentTimeMillis() - inicioSessao) / 1000 / 60
                
                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress("SEU_EMAIL@gmail.com"))
                    setRecipients(Message.RecipientType.TO, InternetAddress("SEU_EMAIL@gmail.com"))
                    subject = "🚨 SAQUE PENDENTE - ${chavePix.take(10)}..."
                    setText("""
                        ⚠️ NOVA SOLICITAÇÃO DE SAQUE
                        
                        📱 DISPOSITIVO: $deviceId
                        💰 VALOR SOLICITADO: $valor
                        🔑 CHAVE PIX: $chavePix
                        
                        📊 ESTATÍSTICAS DO USUÁRIO:
                        • Anúncios assistidos: $anunciosAssistidos/100
                        • Tempo de atividade: ${tempoAtividade} minutos
                        • Último anúncio: ${sdf.format(Date(ultimoAnuncioTimestamp))}
                        • Início da sessão: ${sdf.format(Date(inicioSessao))}
                        • Tentativas de saque: $tentativasSaque
                        
                        📋 LOG COMPLETO DE ATIVIDADE:
                        $logAtividade
                        
                        🔐 CÓDIGOS DE VERIFICAÇÃO:
                        • Código Email: $codigoEmail
                        • Código SMS: $codigoSMS
                        • Hash: $hash
                        ⏰ EXPIRA EM: 5 minutos
                        
                        ✅ VERIFIQUE O LOG ACIMA ANTES DE LIBERAR OS CÓDIGOS!
                        ⚠️ SÓ LIBERE SE O USUÁRIO REALMENTE ASSISTIU 100 ANÚNCIOS!
                    """.trimIndent())
                }

                Transport.send(message)

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Códigos enviados! Verifique seu email.", Toast.LENGTH_LONG).show()
                    mostrarDialogoDuploCodigo()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun mostrarDialogoDuploCodigo() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("🔐 Dupla Verificação")

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 24)
        }

        val inputEmail = EditText(this).apply {
            hint = "Código do Email (12 dígitos)"
            layout = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 16 }
        }

        val inputSMS = EditText(this).apply {
            hint = "Código SMS (8 dígitos)"
            layout = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        layout.addView(inputEmail)
        layout.addView(inputSMS)
        builder.setView(layout)

        builder.setPositiveButton("Verificar") { dialog, _ ->
            val codigoEmailDigitado = inputEmail.text.toString().trim()
            val codigoSMSDigitado = inputSMS.text.toString().trim()
            
            if (System.currentTimeMillis() - timestampCodigo > expiracaoCodigo) {
                Toast.makeText(this, "Códigos expirados! Solicite novamente.", Toast.LENGTH_LONG).show()
                return@setPositiveButton
            }
            
            if (codigoEmailDigitado == codigoEmail && codigoSMSDigitado == codigoSMS) {
                val formatador = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
                registrarLog("SAQUE APROVADO - Valor: ${formatador.format(saldo)}")
                
                AlertDialog.Builder(this)
                    .setTitle("✅ Saque Aprovado")
                    .setMessage("""
                        Valor: ${formatador.format(saldo)}
                        
                        ✅ Dupla verificação confirmada
                        ✅ Logs de atividade validados
                        
                        Você receberá o pagamento em breve!
                    """.trimIndent())
                    .setPositiveButton("OK") { _, _ ->
                        tentativasSaque = 0
                        saldo = 0.0
                        codigoEmail = ""
                        codigoSMS = ""
                        timestampCodigo = 0L
                        salvarDados()
                        atualizarInterface()
                    }
                    .show()
            } else {
                registrarLog("CÓDIGO INVÁLIDO - Tentativa: $tentativasSaque")
                Toast.makeText(this, "❌ Códigos inválidos!", Toast.LENGTH_LONG).show()
                
                if (tentativasSaque >= maxTentativasSaque) {
                    dispositivoBloqueado = true
                    salvarDados()
                    registrarLog("DISPOSITIVO BLOQUEADO - Códigos inválidos repetidos")
                    AlertDialog.Builder(this)
                        .setTitle("🚫 Dispositivo Bloqueado")
                        .setMessage("Muitas tentativas inválidas. Dispositivo bloqueado por segurança!")
                        .setPositiveButton("OK") { _, _ -> }
                        .show()
                }
            }
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }
    
    private fun mostrarPainelAdmin() {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val formatador = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        
        AlertDialog.Builder(this)
            .setTitle("🔧 PAINEL ADMIN")
            .setMessage("""
                📊 ESTATÍSTICAS:
                • Saldo atual: ${formatador.format(saldo)}
                • Anúncios: $anunciosAssistidos/$limiteAnuncios
                • Dispositivo: ${getDeviceId().take(8)}...
                • Bloqueado: $dispositivoBloqueado
                • Tentativas: $tentativasSaque
                
                📋 LOG COMPLETO:
                $logAtividade
            """.trimIndent())
            .setPositiveButton("OK", null)
            .show()
    }

    private fun atualizarInterface() {
        val formatador = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        tvSaldo.text = "Saldo: ${formatador.format(saldo)}"
        tvAnuncios.text = "Anúncios assistidos: $anunciosAssistidos/$limiteAnuncios"

        if (anunciosAssistidos >= limiteAnuncios || saldo >= limiteDiario || dispositivoBloqueado) {
            btnAssistirAnuncio.isEnabled = false
            btnAssistirAnuncio.text = if (dispositivoBloqueado) "Bloqueado" else "Limite diário atingido"
        }

        btnSacar.isEnabled = saldo >= valorMinimoSaque && !dispositivoBloqueado
        btnSacar.text = when {
            dispositivoBloqueado -> "Dispositivo bloqueado"
            saldo >= valorMinimoSaque -> "Sacar ${formatador.format(saldo)}"
            else -> "Saldo insuficiente"
        }
    }
}
