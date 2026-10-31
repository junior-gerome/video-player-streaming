# Python 3.11 : pydub a besoin du module audioop (retiré de Python 3.13)
FROM python:3.11-slim
RUN apt-get update && apt-get install -y --no-install-recommends ffmpeg \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Voix Piper : téléchargées dans /voices (vérifiez la licence de chaque voix)
RUN mkdir -p /voices && cd /voices && \
    for v in en_US-lessac-medium fr_FR-siwis-medium es_ES-davefx-medium \
             de_DE-thorsten-medium ar_JO-kareem-medium pt_BR-faber-medium; do \
      python -m piper.download_voices "$v"; \
    done

COPY main.py .
ENV VOICES_DIR=/voices
EXPOSE 8000
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
