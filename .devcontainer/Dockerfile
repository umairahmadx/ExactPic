FROM mcr.microsoft.com/devcontainers/java:1-17-bookworm

ARG ANDROID_API_LEVEL=35
ARG ANDROID_BUILD_TOOLS=35.0.0
ARG ANDROID_CMDLINE_TOOLS=13114758
ARG KOTLIN_VERSION=2.1.21

ENV ANDROID_HOME=/opt/android-sdk \
    ANDROID_SDK_ROOT=/opt/android-sdk \
    PATH=/opt/kotlin/bin:/opt/android-sdk/cmdline-tools/latest/bin:/opt/android-sdk/platform-tools:/opt/android-sdk/emulator:$PATH

USER root

RUN rm -f /etc/apt/sources.list.d/yarn.list /etc/apt/sources.list.d/yarn.sources \
    && apt-get update \
    && apt-get install -y --no-install-recommends \
        unzip \
        wget \
        lib32stdc++6 \
        lib32z1 \
        libc6-i386 \
        qemu-kvm \
    && rm -rf /var/lib/apt/lists/* \
    && mkdir -p "${ANDROID_HOME}/cmdline-tools" \
    && wget -q "https://dl.google.com/android/repository/commandlinetools-linux-${ANDROID_CMDLINE_TOOLS}_latest.zip" -O /tmp/android-sdk.zip \
    && unzip -q /tmp/android-sdk.zip -d "${ANDROID_HOME}/cmdline-tools" \
    && mv "${ANDROID_HOME}/cmdline-tools/cmdline-tools" "${ANDROID_HOME}/cmdline-tools/latest" \
    && rm /tmp/android-sdk.zip \
    && yes | sdkmanager --licenses >/dev/null || true \
    && sdkmanager "platform-tools" "platforms;android-${ANDROID_API_LEVEL}" "build-tools;${ANDROID_BUILD_TOOLS}" "emulator" \
    && chown -R vscode:vscode "${ANDROID_HOME}" \
    && chmod -R a+rX "${ANDROID_HOME}"

RUN printf '%s\n' \
    'export ANDROID_HOME=/opt/android-sdk' \
    'export ANDROID_SDK_ROOT=/opt/android-sdk' \
    'export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH"' \
    'export PATH="/opt/kotlin/bin:$PATH"' \
    > /etc/profile.d/android-sdk.sh

RUN wget -q "https://github.com/JetBrains/kotlin/releases/download/v${KOTLIN_VERSION}/kotlin-compiler-${KOTLIN_VERSION}.zip" -O /tmp/kotlin.zip \
    && unzip -q /tmp/kotlin.zip -d /opt \
    && mv /opt/kotlinc /opt/kotlin \
    && rm /tmp/kotlin.zip \
    && chown -R vscode:vscode /opt/kotlin

USER vscode