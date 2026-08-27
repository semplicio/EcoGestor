# EcoGestor

Aplicativo mobile de economia sustentável para MEIs, pequenos empreendedores e empresas.

O EcoGestor transforma registros de consumo em indicadores financeiros e ambientais para ajudar o negócio a reduzir desperdícios, economizar recursos e acompanhar metas de sustentabilidade.

## Versão atual — 0.2.0

A versão 0.2.0 transforma o protótipo visual inicial em um MVP funcional.

### Funções disponíveis

- cadastro e edição dos dados da empresa/MEI;
- cadastro de consumos de energia elétrica, água, combustível e materiais;
- informação de quantidade, valor pago e período de cada lançamento;
- histórico real de lançamentos;
- exclusão de lançamentos;
- criação, edição e exclusão de metas sustentáveis;
- acompanhamento percentual das metas;
- persistência local: empresa, consumos e metas permanecem salvos após fechar o aplicativo;
- dashboard calculado com os dados cadastrados pelo usuário;
- comparação do mês atual com o mês anterior;
- cálculo de economia mensal e projeção anual;
- Índice EcoGestor calculado dinamicamente;
- recomendações geradas a partir do histórico de consumo;
- estados vazios no lugar dos antigos dados fictícios.

Nesta etapa os dados ficam armazenados no próprio aparelho. Banco estruturado, autenticação, nuvem, leitura automática de contas e integração com a plataforma web fazem parte das próximas evoluções.

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

Instale os componentes Android utilizados pelo projeto:

```bash
sdkmanager "platforms;android-37.0" "build-tools;37.0.0" "platform-tools"
```

Aceite as licenças, se necessário:

```bash
yes | sdkmanager --licenses
```

## Build local

Na raiz do projeto, usando Gradle instalado no sistema:

```bash
gradle clean :app:assembleDebug
```

Ou gere/use o Gradle Wrapper:

```bash
gradle wrapper --gradle-version 9.5.0
chmod +x gradlew
./gradlew clean :app:assembleDebug
```

APK gerado:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Para instalar ou atualizar no aparelho via ADB:

```bash
adb devices
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

O parâmetro `-r` atualiza o aplicativo existente e preserva os dados locais.

Para apagar todos os dados de teste sem desinstalar:

```bash
adb shell pm clear br.com.autombot.ecogestor
```

## Build no GitHub

O workflow `.github/workflows/android-build.yml` executa automaticamente o build em pushes para `main` e pode ser iniciado manualmente na aba **Actions**.

Após um build bem-sucedido, o APK fica disponível no artefato:

```text
EcoGestor-debug
```

## Próximas etapas

- banco local com Room e histórico mais avançado;
- edição de lançamentos;
- gráficos mensais e anuais;
- categorias e subcategorias personalizadas;
- leitura de contas por foto/PDF;
- notificações e alertas de consumo;
- autenticação e backup/sincronização em nuvem;
- relatórios e exportação;
- integração com futura plataforma web.

## Identidade

**EcoGestor** — Gerencie. Economize. Faça crescer.

Seu negócio. Seu futuro. Sustentável.
