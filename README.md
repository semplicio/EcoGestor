# EcoGestor

Aplicativo mobile de economia sustentável para MEIs, pequenos empreendedores e empresas.

O EcoGestor transforma dados simples de consumo em indicadores financeiros e ambientais para ajudar o negócio a reduzir desperdícios, economizar recursos e acompanhar metas de sustentabilidade.

## MVP Android

A primeira versão do aplicativo inclui a base para:

- dashboard com resumo mensal;
- acompanhamento de energia, água, combustível e materiais;
- cálculo de economia estimada;
- Índice EcoGestor;
- metas de redução de consumo;
- recomendações de economia sustentável;
- estrutura preparada para evolução futura com persistência local, autenticação, leitura de contas e sincronização com a plataforma web.

## Stack

- Kotlin
- Jetpack Compose
- Material 3
- Android Gradle Plugin 9.3.0
- Kotlin 2.3.21
- Compose BOM 2026.08.00
- compileSdk / targetSdk 37

## Build local

O projeto usa Gradle 9.5.x. Caso o wrapper ainda não esteja presente no clone local, gere-o uma vez com Gradle 9.5.0:

```bash
gradle wrapper --gradle-version 9.5.0
chmod +x gradlew
./gradlew clean assembleDebug
```

APK de debug:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Para instalar em um aparelho conectado via ADB:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Identidade

**EcoGestor** — Gerencie. Economize. Faça crescer.

Seu negócio. Seu futuro. Sustentável.
