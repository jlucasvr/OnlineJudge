FROM gcc:14-bookworm

RUN useradd --uid 10001 --create-home --shell /usr/sbin/nologin sandbox
WORKDIR /sandbox
USER sandbox
