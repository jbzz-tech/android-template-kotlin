# Android Template Kotlin

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9-blue.svg)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Issues](https://img.shields.io/github/issues/jbzz-tech/android-template-kotlin)](https://github.com/jbzz-tech/android-template-kotlin/issues)

> Projeto template Android em Kotlin — ponto de partida para apps leves e bem organizados.

Sumário
- Sobre
- Demo / Capturas
- Principais funcionalidades
- Requisitos
- Instalação & Execução
- Estrutura do projeto
- Guia rápido de código
- Testes
- Contribuição
- Versionamento
- Licença
- Contato

Sobre
------
Este repositório é um template para iniciar aplicações Android escritas em Kotlin. Ele contém uma Activity principal simples, patterns recomendados e scripts de configuração para acelerar o desenvolvimento.

Demo / Capturas
---------------
> Insira aqui um GIF ou imagem demonstrando o app em execução.

![screenshot-placeholder](docs/screenshot.png)

Principais funcionalidades
-------------------------
- Configuração mínima pronta para compilar
- Exemplo de MainActivity com ciclo de vida básico
- Estrutura de diretórios clara
- Boas práticas de build e versão

Requisitos
---------
- Android Studio (Arctic Fox ou superior recomendado)
- JDK 11+
- Gradle (wrapper incluído)
- Dispositivo/emulador Android API 21+

Instalação & Execução
---------------------
1. Clone o repositório:

   git clone https://github.com/jbzz-tech/android-template-kotlin.git

2. Abra o projeto no Android Studio (File → Open) e aguarde a sincronização do Gradle.
3. Conecte um dispositivo ou configure um emulador.
4. Execute (Run) o módulo `app`.

Build pela linha de comando

- Debug:

  ./gradlew assembleDebug

- Release (com assinaturas configuradas):

  ./gradlew assembleRelease

Estrutura do projeto
--------------------
Exemplo resumido:

- app/
  - src/main/java/com/exemplo/app/  -> código fonte (Activities, ViewModels, etc.)
  - src/main/res/                    -> recursos (layouts, strings, drawables)
  - src/androidTest/                 -> testes instrumentados
  - src/test/                        -> testes unitários
- build.gradle (nível de projeto)
- settings.gradle

Guia rápido de código
---------------------
A Activity principal está em:

[MainActivity.kt](https://github.com/jbzz-tech/android-template-kotlin/blob/main/app/src/main/java/com/exemplo/app/MainActivity.kt)

Trecho de uso (exemplo simplificado):

```kotlin
// Exemplo: iniciar uma nova Activity
val intent = Intent(this, AnotherActivity::class.java)
startActivity(intent)
```

Boas práticas sugeridas
- Separe lógica de UI em ViewModels
- Use coroutines para trabalho assíncrono (Kotlinx.coroutines)
- Centralize strings no resources
- Configure ProGuard/R8 para builds de release

Testes
------
- Testes unitários com JUnit
- Testes instrumentados com Espresso

Para executar os testes:

./gradlew test
./gradlew connectedAndroidTest

Contribuição
------------
Contribuições são bem-vindas! Siga estas etapas:

1. Fork este repositório
2. Crie uma branch: `git checkout -b feat/minha-nova-funcionalidade`
3. Faça commits claros e pequenos
4. Abra um Pull Request descrevendo as mudanças

Por favor, abra uma issue antes de implementar recursos grandes para discutirmos o design.

Versionamento
-------------
Utilizamos SemVer para versionamento. Veja o arquivo CHANGELOG.md para histórico de alterações.

Roadmap
-------
- Integração com arquitetura MVVM
- Exemplos de uso de Jetpack Compose
- Template de CI (Github Actions)

Problemas comuns & Soluções
--------------------------
- Erro de sincronização do Gradle: verifique a versão do plugin Android e do Gradle wrapper.
- Problemas de assinatura: confirme as propriedades em `keystore.properties` ou configure variáveis de ambiente.

Licença
-------
Este projeto está licenciado sob a Licença MIT — detalhes no arquivo LICENSE.

Acknowledgements
----------------
- Projetos e bibliotecas open-source usados como referência

Contato
-------
Criado por jbzz-tech — abra uma issue ou envie um PR.

---

Se quiser, eu posso:
- Adicionar um arquivo LICENSE (MIT) automaticamente
- Criar um template de CONTRIBUTING.md ou ISSUE_TEMPLATE
- Gerar um CHANGELOG.md inicial
