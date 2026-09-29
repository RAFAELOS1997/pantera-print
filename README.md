# Pantera Print
App Android em português para diagnosticar e depois imprimir diretamente em mini impressoras térmicas do ecossistema Frog To Sea / iPrint.

## v0.1 — Diagnóstico BLE
- Busca dispositivos BLE
- Conecta via GATT
- Descobre serviços, características e descritores
- Exibe UUIDs e propriedades do canal
- Interface em português

## Como testar
Abra no Android Studio, compile e instale. Ligue a impressora, toque em **Procurar impressora**, selecione o dispositivo correspondente e envie o relatório GATT.

A v0.1 não envia bytes de impressão deliberadamente: primeiro identificamos a característica correta, sem assumir um protocolo incompatível.

Próximo: driver, imagens monocromáticas/dithering, editor de etiquetas, QR Code/código de barras, modelos e impressão em lote.