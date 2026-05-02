# Android Template Kotlin

Um template limpo e moderno para iniciar projetos Android com Kotlin.

## 📋 Descrição

Este repositório contém um template base para desenvolvimento de aplicações Android utilizando Kotlin como linguagem principal. O projeto está estruturado seguindo boas práticas de desenvolvimento e padrões arquiteturais recomendados pelo Android.

## 🛠 Tecnologias

- **Linguagem**: Kotlin
- **Platform**: Android
- **Gradle**: Build system
- **AppCompat**: Support library para compatibilidade

## 📁 Estrutura do Projeto

```
android-template-kotlin/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/exemplo/app/
│   │   │   │   └── MainActivity.kt
│   │   │   ├── res/
│   │   │   └── AndroidManifest.xml
│   │   └── ...
│   └── build.gradle
├── .gitignore
├── build.gradle
└── settings.gradle
```

## 🚀 Como Começar

### Pré-requisitos

- Android Studio (versão recente)
- Java Development Kit (JDK 8 ou superior)
- Android SDK

### Instalação

1. Clone o repositório:
```bash
git clone https://github.com/jbzz-tech/android-template-kotlin.git
cd android-template-kotlin
```

2. Abra o projeto no Android Studio

3. Sincronize as dependências do Gradle:
```bash
./gradlew sync
```

4. Execute a aplicação em um emulador ou dispositivo físico

## 📝 Componentes Principais

### MainActivity
A atividade principal da aplicação, que serve como ponto de entrada para a aplicação Android.

```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}
```

## 🔧 Configuração

Edite as seguintes configurações conforme necessário:

- **Package Name**: `com.exemplo.app` → seu package desejado
- **App Name**: Altere em `res/values/strings.xml`
- **Target SDK**: Configure em `app/build.gradle`

## 📦 Dependências

O projeto utiliza as seguintes dependências principais:

- AndroidX AppCompat
- Android Material Design (opcional)
- JUnit para testes

## 🧪 Testes

Para executar testes:

```bash
./gradlew test
```

## 📄 Licença

Este projeto está disponível sob licença livre. Veja o arquivo LICENSE para mais detalhes.

## 👤 Autor

**jbzz-tech**

- GitHub: [@jbzz-tech](https://github.com/jbzz-tech)

## 🤝 Contribuições

Contribuições são bem-vindas! Sinta-se à vontade para abrir issues e enviar pull requests.

## 📞 Suporte

Se tiver dúvidas ou encontrar problemas, abra uma issue no repositório.

---

**Criado em**: 2026-05-02
