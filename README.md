# EcoGestor

Aplicativo Android para economia sustentável, controle financeiro doméstico e gestão de pequenos negócios.

O EcoGestor possui dois modos independentes:

- **Casa** — renda, gastos, contas recorrentes, assinaturas, metas financeiras, orçamento por categoria, gás de cozinha, histórico e calculadoras;
- **Negócio** — consumos, custos, metas sustentáveis, indicadores e recomendações para MEI e pequenas empresas.

Os dados dos dois modos ficam separados e o usuário pode alternar entre Casa e Negócio pelo próprio aplicativo.

## Versão atual — 0.3.0

### Modo Casa

- configuração do perfil doméstico e renda mensal planejada;
- cadastro de entradas e saídas;
- categorias para salário, renda extra, moradia, supermercado, feira, gás, energia, água, internet, celular, TV, assinaturas, academia, saúde, transporte, educação, delivery, lazer, farmácia, roupas, manutenção, presentes e outros;
- busca de lançamentos e filtro por entradas/saídas;
- edição e exclusão de movimentos;
- dashboard com renda, gastos e saldo do mês;
- Raio-X dos gastos por categoria;
- comparação com mês anterior;
- oportunidades de economia geradas a partir do histórico;
- contas recorrentes com dia de vencimento, ativação/pausa e status de pagamento mensal;
- ao marcar uma conta como paga, o gasto é lançado automaticamente no mês;
- assinaturas mensais com custo mensal e anual;
- lançamento de assinatura no mês;
- orçamento mensal por categoria com alerta visual ao atingir ou ultrapassar o limite;
- metas financeiras com valor-alvo, valor guardado, prazo e progresso;
- controle de botijão de gás, duração, custo médio e previsão aproximada da próxima troca;
- histórico mensal de entradas, saídas e saldo;
- exportação/compartilhamento de resumo em formato CSV/texto;
- persistência local dos dados no aparelho.

### Calculadoras EcoGestor

- calculadora comum (+, −, ×, ÷ e percentual);
- orçamento mensal;
- desconto;
- parcelamento com juros mensais opcionais;
- custo mensal convertido para custo anual e de cinco anos;
- consumo e custo de energia elétrica;
- custo de viagem por combustível;
- custo diário e mensal do gás de cozinha.

### Modo Negócio

- cadastro e edição dos dados da empresa/MEI;
- cadastro de consumos de energia elétrica, água, combustível e materiais;
- quantidade, valor pago e período de cada lançamento;
- histórico de lançamentos;
- exclusão de lançamentos;
- criação, edição e exclusão de metas sustentáveis;
- dashboard calculado com os dados cadastrados;
- comparação do mês atual com o anterior;
- cálculo de economia mensal e projeção anual;
- Índice EcoGestor dinâmico;
- recomendações com base no histórico de consumo.

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

## Pré-requisitos

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

Na raiz do projeto:

```bash
git pull origin main
./gradlew clean :app:assembleDebug
```

Se o clone ainda não possuir o wrapper:

```bash
gradle wrapper --gradle-version 9.5.0
chmod +x gradlew
./gradlew clean :app:assembleDebug
```

APK gerado:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Instalação/atualização via ADB:

```bash
adb devices
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Para apagar todos os dados de teste:

```bash
adb shell pm clear br.com.autombot.ecogestor
```

## Build no GitHub

O workflow `.github/workflows/android-build.yml` executa o build automaticamente em pushes e pull requests para `main`.

Após um build bem-sucedido, o APK fica disponível no artefato:

```text
EcoGestor-debug
```

## Próximas evoluções

- banco local com Room para volumes maiores de dados;
- gráficos mensais e anuais mais avançados;
- categorias e subcategorias personalizadas;
- leitura de contas e comprovantes por foto/PDF;
- notificações de vencimento e alertas de orçamento;
- autenticação, backup e sincronização em nuvem;
- relatório PDF;
- integração com a futura plataforma web.

## Identidade

**EcoGestor** — Gerencie. Economize. Faça crescer.

Seu dinheiro. Seu negócio. Seu futuro. Sustentável.
