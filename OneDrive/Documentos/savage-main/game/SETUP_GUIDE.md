# Guia de Configuração - Game Events

## Passo a Passo para Configurar um Novo Mapa

### 1. Preparar o Mundo

Crie o mundo do mapa no servidor com o nome do evento (ex: `sumo`).

### 2. Iniciar Configuração

```
/game load <nome_do_mapa>
```

Exemplo: `/game load arena_principal`

### 3. Configurar Posições

#### Spawn Principal (Obrigatório)
Onde os jogadores nascem ao entrar no evento.

1. Vá até a posição desejada
2. Execute: `/game setpos spawn`
3. Copie as coordenadas exibidas

#### Spawn Equipe Vermelha (Opcional)
Para eventos com equipes.

1. Vá até a posição da equipe vermelha
2. Execute: `/game setpos red_spawn`
3. Copie as coordenadas exibidas

#### Spawn Equipe Azul (Opcional)
Para eventos com equipes.

1. Vá até a posição da equipe azul
2. Execute: `/game setpos blue_spawn`
3. Copie as coordenadas exibidas

### 4. Criar Arquivo de Configuração

Crie o arquivo em: `plugins/Game/maps/sumo/<nome_do_mapa>.yml`

```yaml
name: "Arena Principal"
id: 1

spawn:
  x: 0
  y: 100
  z: 0
  yaw: 0.0
  pitch: 0.0

red_spawn:
  x: 10
  y: 100
  z: 0
  yaw: -90.0
  pitch: 0.0

blue_spawn:
  x: -10
  y: 100
  z: 0
  yaw: 90.0
  pitch: 0.0

border:
  center:
    x: 0
    z: 0
  size: 100

settings:
  min_y: 50
  max_players: 16
  min_players: 2
```

### 5. Recarregar o Evento

```
/game sumo reload
```

### 6. Testar

```
/game tp <nome_do_mapa>
/evento
```

## Comandos Úteis

### Teleporte Rápido
```
/game tp <mapa>
```

### Ver Mapas Carregados
```
/game sumo listmaps
```

### Ver Informações do Evento
```
/game sumo info
```

### Recarregar Após Mudanças
```
/game sumo reload
```

## Configurações Importantes

### min_y
Altura mínima antes de considerar morte por queda.
- Padrão: 50
- Ajuste conforme a altura da sua arena

### max_players / min_players
Número máximo e mínimo de jogadores por sala.
- Padrão: 2-16
- Ajuste conforme o tamanho da arena

### border
Define o tamanho da borda do mundo.
- center: Centro da borda (x, z)
- size: Tamanho total da borda

## Troubleshooting

### Mapa não aparece na lista
- Verifique se o arquivo .yml está na pasta correta
- Use `/game sumo reload` para recarregar

### Jogadores não nascem no spawn correto
- Verifique as coordenadas no arquivo .yml
- Certifique-se de que o mundo existe
- Use `/game tp <mapa>` para testar

### Scoreboard não atualiza
- Verifique se o jogador está na arena
- Recarregue o evento com `/game sumo reload`

## Exemplo Completo

```bash
# 1. Carregar mapa
/game load arena_pvp

# 2. Ir para posição do spawn
/tp 0 100 0

# 3. Definir spawn
/game setpos spawn

# 4. Ir para posição vermelha
/tp 10 100 0

# 5. Definir spawn vermelho
/game setpos red_spawn

# 6. Ir para posição azul
/tp -10 100 0

# 7. Definir spawn azul
/game setpos blue_spawn

# 8. Criar arquivo arena_pvp.yml com as coordenadas

# 9. Recarregar
/game sumo reload

# 10. Testar
/game tp arena_pvp
/evento
```
