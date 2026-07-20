FROM eclipse-temurin:21-jre-noble
LABEL authors="zhaomh"

WORKDIR /app

# 安装 Playwright 无头 Chromium 所需系统库及中文字体
RUN apt-get update && apt-get install -y --no-install-recommends \
    libnss3 \
    libnspr4 \
    libdbus-1-3 \
    libatk1.0-0 \
    libatk-bridge2.0-0 \
    libcups2 \
    libdrm2 \
    libxkbcommon0 \
    libxcomposite1 \
    libxdamage1 \
    libxfixes3 \
    libxrandr2 \
    libgbm1 \
    libpango-1.0-0 \
    libcairo2 \
    libasound2 \
    libatspi2.0-0 \
    fonts-noto-cjk \
    fonts-noto-color-emoji \
    && rm -rf /var/lib/apt/lists/*

RUN mkdir -p /app/plugins

COPY ./build/libs/MolinkBot-1.0.0-all.jar /app/MolinkBot.jar
COPY ./plugins/ /app/plugins/

ENV HOST=0.0.0.0
ENV PORT=3001
ENV TOKEN=""

EXPOSE 8079 8078

ENTRYPOINT ["java", "-Xmx1024m", "-Xms512m", "-jar", "/app/MolinkBot.jar"]