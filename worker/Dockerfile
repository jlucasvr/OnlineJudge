FROM python:3.12-slim

# Instala compiladores e runtime necessários
RUN apt-get update && apt-get install -y --no-install-recommends \
        gcc \
        g++ \
        default-jdk-headless \
        && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

# Cria estrutura base do volume (será sobrescrita pelo bind mount)
RUN mkdir -p /data/submissions /data/testcases /data/compile-errors /data/temp/exec

CMD ["python", "-u", "main.py"]
