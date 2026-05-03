package com.exemplo.app

import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds

class AdManager(private val context: Context) {
    
    // KEYS DE TESTE ADMOB
    private val ADMOB_APP_ID = "ca-app-pub-3940256099942544~3347511713"
    private val ADMOB_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"
    
    // KEYS DE TESTE UNITY ADS
    private val UNITY_GAME_ID = "1486550"
    private val UNITY_REWARDED_ID = "rewardedVideo"
    
    // KEYS DE TESTE APPLOVIN
    private val APPLOVIN_SDK_KEY = "YOUR_SDK_KEY"
    
    // KEYS DE TESTE FACEBOOK
    private val FACEBOOK_PLACEMENT_ID = "YOUR_PLACEMENT_ID"
    
    // KEYS DE TESTE ADCOLONY
    private val ADCOLONY_APP_ID = "YOUR_APP_ID"
    private val ADCOLONY_ZONE_ID = "YOUR_ZONE_ID"
    
    private var currentAdNetwork: AdNetwork = AdNetwork.ADMOB
    private var networkRotationIndex = 0
    private val adNetworks = listOf(
        AdNetwork.ADMOB,
        AdNetwork.UNITY,
        AdNetwork.APPLOVIN,
        AdNetwork.FACEBOOK,
        AdNetwork.ADCOLONY
    )
    
    private var admobRewardedAd: RewardedAd? = null
    
    enum class AdNetwork {
        ADMOB, UNITY, APPLOVIN, FACEBOOK, ADCOLONY
    }
    
    fun initializeAllNetworks() {
        Log.d("AdManager", "Inicializando todas as redes de anúncios...")
        
        // Inicializa AdMob
        MobileAds.initialize(context) {
            Log.d("AdManager", "AdMob inicializado")
            carregarAdMob()
        }
        
        // Inicializa Unity Ads (modo teste)
        UnityAds.initialize(
            context,
            UNITY_GAME_ID,
            true, // Modo teste ativado
            object : IUnityAdsInitializationListener {
                override fun onInitializationComplete() {
                    Log.d("AdManager", "Unity Ads inicializado")
                }
                override fun onInitializationFailed(p0: UnityAds.UnityAdsInitializationError?, p1: String?) {
                    Log.e("AdManager", "Unity Ads falhou: $p1")
                }
            }
        )
        
        Log.d("AdManager", "Todas as redes inicializadas em modo teste")
    }
    
    private fun carregarAdMob() {
        RewardedAd.load(
            context,
            ADMOB_REWARDED_ID,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    admobRewardedAd = ad
                    Log.d("AdManager", "AdMob Rewarded carregado")
                }
                override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                    Log.e("AdManager", "AdMob falhou: ${error.message}")
                }
            }
        )
    }
    
    fun getNextAdNetwork(): AdNetwork {
        val network = adNetworks[networkRotationIndex % adNetworks.size]
        networkRotationIndex++
        return network
    }
    
    fun showRewardedAd(callback: () -> Unit) {
        val network = getNextAdNetwork()
        
        when (network) {
            AdNetwork.ADMOB -> showAdMobRewarded(callback)
            AdNetwork.UNITY -> showUnityRewarded(callback)
            AdNetwork.APPLOVIN -> showAppLovinRewarded(callback)
            AdNetwork.FACEBOOK -> showFacebookRewarded(callback)
            AdNetwork.ADCOLONY -> showAdColonyRewarded(callback)
        }
    }
    
    private fun showAdMobRewarded(callback: () -> Unit) {
        admobRewardedAd?.let { ad ->
            ad.show(context) { reward ->
                Log.d("AdManager", "AdMob: Recompensa recebida: ${reward.amount}")
                callback()
            }
            // Recarregar para o próximo
            carregarAdMob()
        } ?: run {
            Log.d("AdManager", "AdMob não carregado, usando simulação")
            // Simulação para teste
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                callback()
            }, 2000)
        }
    }
    
    private fun showUnityRewarded(callback: () -> Unit) {
        if (UnityAds.isInitialized()) {
            UnityAds.load(
                UNITY_REWARDED_ID,
                object : IUnityAdsLoadListener {
                    override fun onUnityAdsAdLoaded(p0: String?) {
                        UnityAds.show(
                            context as android.app.Activity,
                            UNITY_REWARDED_ID,
                            object : IUnityAdsShowListener {
                                override fun onUnityAdsShowComplete(p0: String?, p1: UnityAds.UnityAdsShowCompletionState?) {
                                    if (p1 == UnityAds.UnityAdsShowCompletionState.COMPLETED) {
                                        Log.d("AdManager", "Unity Ads: Recompensa recebida")
                                        callback()
                                    }
                                }
                                override fun onUnityAdsShowFailure(p0: String?, p1: UnityAds.UnityAdsError?, p2: String?) {
                                    Log.e("AdManager", "Unity Ads falhou: $p2")
                                    // Fallback para simulação
                                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                        callback()
                                    }, 2000)
                                }
                                override fun onUnityAdsShowStart(p0: String?) {}
                                override fun onUnityAdsShowClick(p0: String?) {}
                            }
                        )
                    }
                    override fun onUnityAdsFailedToLoad(p0: String?, p1: UnityAds.UnityAdsLoadError?, p2: String?) {
                        Log.e("AdManager", "Unity Ads load falhou: $p2")
                        // Simulação para teste
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            callback()
                        }, 2000)
                    }
                }
            )
        } else {
            // Simulação para teste
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                callback()
            }, 2000)
        }
    }
    
    private fun showAppLovinRewarded(callback: () -> Unit) {
        // Simulação para teste
        Log.d("AdManager", "AppLovin: Simulação de anúncio")
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            callback()
        }, 2000)
    }
    
    private fun showFacebookRewarded(callback: () -> Unit) {
        // Simulação para teste
        Log.d("AdManager", "Facebook: Simulação de anúncio")
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            callback()
        }, 2000)
    }
    
    private fun showAdColonyRewarded(callback: () -> Unit) {
        // Simulação para teste
        Log.d("AdManager", "AdColony: Simulação de anúncio")
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            callback()
        }, 2000)
    }
}
