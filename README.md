# EcoGestor

Aplicativo mobile de economia sustentável para MEIs, pequenos empreendedores e empresas.

O EcoGestor transforma dados simples de consumo em indicadores financeiros e ambientais para ajudar o negócio a reduzir desperdícios, economizar recursos e acompanhar metas de sustentabilidade.

## MVP Android

A primeira versão do aplicativo inclui:

- dashboard com resumo mensal;
- acompanhamento de energia, água, combustível e materiais;
- cálculo de economia estimada;
- Índice EcoGestor;
- metas de redução de consumo;
- recomendações de economia sustentável;
- navegação entre Início, Consumos, Metas e Empresa;
- identidade visual e ícone adaptativo do EcoGestor;
- estrutura preparada para persistência local, autenticação, leitura de contas e sincronização futura com a plataforma web.

## Stack

- Kotlin com suporte integrado do Android Gradle Plugin
- Jetpack Compose
- Material 3
- Android Gradle Plugin 9.3.0
- Kotlin / Compose Compiler 2.3.21
- Compose BOM 2026.08.00
- Gradle 9.5.0
- Java 17
- compileSdk / targetSdk 37

## Pré-requisitos para build local

- JDK 17
- Android SDK Command-line Tools atualizados
- Gradle 9.5.0 ou Android Studio compatível

Para instalar a plataforma usada pelo projeto via linha de comando:

```bash
sdkmanager "platforms;android-37.0" "build-tools;37.0.0" "platform-tools"
```

Aceite as licenças, se necessário:

```bash
yes | sdkmanager --licenses
```

## Build usando Gradle instalado no sistema

Na raiz do projeto:

```bash
gradle clean :app:assembleDebug
```

## Gerar o Gradle Wrapper e usar ./gradlew

Caso o clone ainda não tenha o wrapper:

```bash
gradle wrapper --gradle-version 9.5.0
chmod +x gradlew
./gradlew clean :app:assembleDebug
```

APK de debug:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Para instalar em um aparelho conectado via ADB:

```bash
adb devices
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Para remover a instalação de teste:

```bash
adb uninstall br.com.autombot.ecogestor
```

## Build no GitHub

O workflow `.github/workflows/android-build.yml` executa o build do APK automaticamente em pushes para `main` e também pode ser iniciado manualmente pela aba **Actions** do repositório.

Ao concluir com sucesso, o workflow publica o artefato:

```text
EcoGestor-debug
```

## Identidade

**EcoGestor** — Gerencie. Economize. Faça crescer.

Seu negócio. Seu futuro. Sustentável.
