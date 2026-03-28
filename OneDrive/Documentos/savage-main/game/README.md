# Game Events - Sistema de Eventos

Sistema de eventos para Minecraft com suporte a múltiplos minigames.

## Comandos

### Para Jogadores

- `/evento` - Entrar na sala de eventos (atualmente Sumo)

### Para Administradores

#### Gerenciamento de Mapas
- `/game load <mapa>` - Carregar um mapa para configuração
- `/game tp <mapa>` - Teleportar para um mapa específico
- `/game setpos <tipo>` - Definir posição no mapa atual
  - Tipos: `spawn`, `red_spawn`, `blue_spawn`
- `/game listpos <mapa>` - Listar todas as posições configuradas do mapa

#### Gerenciamento de Eventos
- `/game <evento> listmaps` - Listar todos os mapas disponíveis do evento
- `/game <evento> reload` - Recarregar o evento e suas salas
- `/game <evento> info` - Ver informações do evento

**Exemplos:**
```
/game load arena_principal
/game tp arena_principal
/game setpos spawn
/game setpos red_spawn
/game setpos blue_spawn
/game listpos arena_principal

/game sumo listmaps
/game sumo reload
/game sumo info
```

## Eventos Disponíveis

### Sumo
- Modo de jogo onde jogadores tentam derrubar uns aos outros da plataforma
- Vence o último jogador que permanecer na arena
- Sistema de scoreboard com kills e deaths
- Botão "Jogar novamente" ao morrer

## Estrutura de Mapas

Os mapas devem estar na pasta `plugins/Game/maps/` com a seguinte estrutura:

```
maps/
  sumo/
    example_map.yml
    arena_principal.yml
```

### Configuração do Mapa (map.yml)

```yaml
# Nome do mapa exibido no jogo
name: "Arena Principal"
id: 1

# Posição onde os jogadores nascem ao entrar no evento
spawn:
  x: 0
  y: 100
  z: 0
  yaw: 0.0
  pitch: 0.0

# Posição do spawn da equipe vermelha (opcional)
red_spawn:
  x: 10
  y: 100
  z: 0
  yaw: -90.0
  pitch: 0.0

# Posição do spawn da equipe azul (opcional)
blue_spawn:
  x: -10
  y: 100
  z: 0
  yaw: 90.0
  pitch: 0.0

# Configurações da borda do mundo
border:
  center:
    x: 0
    z: 0
  size: 100

# Configurações específicas do evento
settings:
  min_y: 50  # Altura mínima antes de considerar morte por queda
  max_players: 16
  min_players: 2
```

## Como Configurar um Novo Mapa

1. Crie o mundo do mapa no servidor
2. Use `/game load <nome_do_mapa>` para iniciar a configuração
3. Vá até a posição desejada e use:
   - `/game setpos spawn` - Define onde os jogadores nascem
   - `/game setpos red_spawn` - Define spawn da equipe vermelha
   - `/game setpos blue_spawn` - Define spawn da equipe azul
4. Copie as coordenadas exibidas para o arquivo `map.yml`
5. Use `/game sumo reload` para recarregar o evento

## Scoreboard

O scoreboard é atualizado automaticamente e mostra:

**Antes do jogo:**
- Data e identificador da arena
- Nome do mapa
- Modo de jogo
- Tempo para início
- Número de jogadores

**Durante o jogo:**
- Kills do jogador
- Deaths do jogador

## Configuração

Arquivo: `config.yml`

```yaml
events:
  sumo:
    enabled: true
    min-players: 2
    max-players: 16
```
