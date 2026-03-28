# Game Events - Sistema de Eventos

Sistema de eventos para Minecraft com suporte a múltiplos minigames.

## Comandos

### Para Jogadores

- `/evento` - Entrar na sala de eventos (atualmente Sumo)

### Para Administradores

- `/game <evento> listmaps` - Listar todos os mapas disponíveis do evento
- `/game <evento> reload` - Recarregar o evento e suas salas
- `/game <evento> info` - Ver informações do evento

**Exemplos:**
```
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
    map.yml
```

## Configuração

Arquivo: `config.yml`

```yaml
events:
  sumo:
    enabled: true
    min-players: 2
    max-players: 16
```
