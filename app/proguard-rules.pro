# ============================================================
# WifiNetworkScanner - Regras ProGuard/R8
# ============================================================
# Objetivo:
# - Permitir minificação/otimização no release.
# - Evitar quebra em Room, Hilt e modelos usados em domínio/exportação.
# - Manter o arquivo preparado para futura camada remota, sem ativar regras
#   desnecessárias enquanto Retrofit/OkHttp/serialização não forem usados.
# ============================================================


# ============================================================
# Atributos usados por bibliotecas com anotações, generics e DI
# ============================================================

-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes RuntimeInvisibleParameterAnnotations
-keepattributes AnnotationDefault


# ============================================================
# Kotlin
# ============================================================
# Mantém metadados Kotlin usados por bibliotecas que leem anotações,
# construtores, sealed classes, data classes e generics.
# ============================================================

-keep class kotlin.Metadata { *; }


# ============================================================
# Room
# ============================================================
# O Room já fornece consumer rules, mas este projeto usa entities,
# relations e projections próprias. Manter esses pacotes evita quebra por
# reflexão/anotações e facilita diagnóstico em release.
# ============================================================

-keep class * extends androidx.room.RoomDatabase { *; }

-keep @androidx.room.Database class * { *; }
-keep @androidx.room.Dao class * { *; }
-keep @androidx.room.Entity class * { *; }

-keep class com.example.wifinetworkscanner.data.local.database.** { *; }
-keep class com.example.wifinetworkscanner.data.local.dao.** { *; }
-keep class com.example.wifinetworkscanner.data.local.entity.** { *; }
-keep class com.example.wifinetworkscanner.data.local.relation.** { *; }
-keep class com.example.wifinetworkscanner.data.local.projection.** { *; }

-keepclassmembers class * {
    @androidx.room.PrimaryKey <fields>;
    @androidx.room.ColumnInfo <fields>;
    @androidx.room.Embedded <fields>;
    @androidx.room.Relation <fields>;
}


# ============================================================
# Hilt / Dagger
# ============================================================
# Hilt/Dagger também fornecem consumer rules, mas estas regras preservam
# classes anotadas e dependências agregadas do Hilt para reduzir risco em
# builds minificados.
# ============================================================

-keep @dagger.Module class * { *; }
-keep @dagger.hilt.InstallIn class * { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }

-keep class hilt_aggregated_deps.** { *; }

-keepclassmembers class * {
    @javax.inject.Inject <init>(...);
    @javax.inject.Inject <fields>;
    @javax.inject.Inject <methods>;
}

-dontwarn dagger.hilt.internal.**
-dontwarn dagger.hilt.android.internal.**


# ============================================================
# Modelos de domínio
# ============================================================
# Estes modelos são pequenos e usados em fluxos de UI, histórico e exportação.
# Manter nomes/membros ajuda se futuramente forem usados em serialização,
# navegação tipada, logs estruturados ou compartilhamento.
# ============================================================

-keep class com.example.wifinetworkscanner.domain.model.** { *; }


# ============================================================
# Enums
# ============================================================
# Mantém métodos usados por valueOf(), values() e mapeamentos string <-> enum.
# ============================================================

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}


# ============================================================
# DataStore Preferences
# ============================================================
# DataStore Preferences não exige keep rules específicas neste projeto.
# Não há Proto DataStore nem classes geradas por protobuf atualmente.
# ============================================================


# ============================================================
# Retrofit / OkHttp / Serialização
# ============================================================
# Atualmente o projeto não possui data/remote, Retrofit, OkHttp, Gson,
# Moshi ou Kotlin Serialization declarados no Version Catalog.
#
# Quando a camada remota for adicionada, incluir regras específicas para:
# - interfaces Retrofit em data/remote/api
# - DTOs em data/remote/dto
# - conversor usado: Moshi, Gson ou kotlinx.serialization
# ============================================================