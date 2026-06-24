\# Wi-Fi Scanner



Aplicativo Android para diagnóstico local de redes Wi-Fi.

O app identifica informações da rede atual, executa varredura de dispositivos alcançáveis na mesma faixa de IP, salva histórico local e permite exportar relatórios em TXT e CSV.



\## Recursos



\* Varredura de dispositivos na rede Wi-Fi atual.

\* Exibição de IP local, gateway provável e faixa analisada.

\* Histórico local de varreduras.

\* Agrupamento do histórico por rede.

\* Detalhes dos dispositivos encontrados.

\* Pesquisa por rede, IP, gateway, dispositivo ou porta.

\* Exportação de relatórios em TXT e CSV.

\* Compartilhamento seguro usando FileProvider.

\* Configurações locais para quantidade máxima de hosts, timeout e paralelismo.

\* Tema claro/escuro com Material 3.

\* Build release assinado, minificado e validado.



\## Arquitetura



O projeto segue uma organização em camadas:



```text

data/

&#x20; local/

&#x20; repository/



di/



domain/

&#x20; model/

&#x20; repository/

&#x20; usecase/

&#x20; validation/



ui/

&#x20; components/

&#x20; navigation/

&#x20; screens/

&#x20; theme/



utils/

&#x20; logger/

&#x20; network/

&#x20; files/

```



Padrões usados:



\* Kotlin.

\* Jetpack Compose.

\* Material 3.

\* MVVM/MVI.

\* Hilt para injeção de dependência.

\* Coroutines e Flow.

\* Room para persistência local.

\* DataStore Preferences para configurações.

\* FileProvider para compartilhamento seguro de relatórios.



\## Permissões



O app pode solicitar ou declarar permissões relacionadas a rede e Wi-Fi:



\* `INTERNET`

\* `ACCESS\_NETWORK\_STATE`

\* `ACCESS\_WIFI\_STATE`

\* `ACCESS\_FINE\_LOCATION`

\* `NEARBY\_WIFI\_DEVICES`



A permissão de localização pode ser necessária porque o Android exige essa permissão para permitir a leitura do nome da rede Wi-Fi em algumas versões do sistema.



\## Privacidade



O Wi-Fi Scanner não possui camada remota nesta versão.



\* O app não envia dados para servidores externos.

\* O app não vende dados.

\* O histórico é salvo localmente no aparelho.

\* As configurações são salvas localmente no aparelho.

\* Relatórios TXT/CSV são criados no cache do app.

\* Relatórios só são compartilhados por ação explícita do usuário.



Use o app apenas em redes próprias ou em redes onde você tenha autorização para realizar diagnóstico.



\## Limitações técnicas



Alguns dispositivos podem estar conectados ao roteador e não responder à varredura por motivos como:



\* Firewall do dispositivo.

\* Isolamento de clientes no roteador.

\* Bloqueio de ICMP/ping.

\* Portas fechadas.

\* Configurações específicas do sistema operacional.



Por isso, a ausência de resposta não significa obrigatoriamente que o dispositivo não está conectado à rede.



\## Build e testes



Comandos principais:



```powershell

.\\gradlew.bat clean

.\\gradlew.bat :app:testDebugUnitTest

.\\gradlew.bat :app:connectedDebugAndroidTest

.\\gradlew.bat :app:assembleDebug

.\\gradlew.bat :app:assembleRelease

.\\gradlew.bat :app:bundleRelease

```



\## Artefatos de release



APK release:



```text

app/build/outputs/apk/release/app-release.apk

```



AAB release:



```text

app/build/outputs/bundle/release/app-release.aab

```



\## Assinatura de release



As credenciais de assinatura devem ficar no `local.properties` e não devem ser versionadas.



Exemplo:



```properties

RELEASE\_STORE\_FILE=release-key.jks

RELEASE\_STORE\_PASSWORD=sua\_senha\_da\_keystore

RELEASE\_KEY\_ALIAS=wifi\_network\_scanner

RELEASE\_KEY\_PASSWORD=sua\_senha\_da\_chave

```



Arquivos sensíveis que não devem ir para Git:



```gitignore

\*.jks

\*.keystore

local.properties

```



\## Status da versão 1.0.0



Validações concluídas:



\* Testes unitários executados com sucesso.

\* Testes instrumentados executados com sucesso em dispositivo físico.

\* APK release gerado com sucesso.

\* AAB release gerado com sucesso.

\* APK release assinado e verificado.

\* R8/ProGuard habilitado no release.

\* FileProvider restrito ao cache de relatórios.

\* Manifest revisado para release.



\## Licença



Projeto em desenvolvimento. Defina uma licença antes de publicação pública.



