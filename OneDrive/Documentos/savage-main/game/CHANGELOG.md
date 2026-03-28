# Changelog - Game Events

## Versão 1.1.0 - Sistema de Configuração de Mapas

### Novos Comandos

#### `/game load <mapa>`
Inicia o processo de configuração de um novo mapa.
```
/game load arena_principal
```

#### `/game tp <mapa>`
Teleporta o administrador para um mapa específico.
```
/game tp arena_classica
```

#### `/game setpos <tipo>`
Define posições importantes no mapa atual.
```
/game setpos spawn
/game setpos red_spawn
/game setpos blue_spawn
```

#### `/game listpos <mapa>`
Lista todas as posições configuradas de um mapa.
```
/game listpos arena_gigante
```

### Configurações de Mapa

Cada mapa agora suporta as seguintes configurações:

#### Posições
- **spawn**: Onde os jogadores nascem ao entrar no evento (obrigatório)
- **red_spawn**: Spawn da equipe vermelha (opcional)
- **blue_spawn**: Spawn da equipe azul (opcional)

#### Configurações de Gameplay
- **min_y**: Altura mínima antes de considerar morte por queda
- **max_players**: Número máximo de jogadores
- **min_players**: Número mínimo de jogadores para iniciar

#### Borda do Mundo
- **center**: Centro da borda (x, z)
- **size**: Tamanho total da borda

### Scoreboard Implementado

O scoreboard agora exibe informações em tempo real:

**Fase de Espera:**
- Data e identificador da arena
- Nome do mapa
- Modo de jogo
- Tempo para início
- Número de jogadores conectados

**Durante o Jogo:**
- Kills do jogador
- Deaths do jogador

### Mapas de Exemplo

Incluídos 3 mapas de exemplo:
1. **example_map.yml** - Template básico para criar novos mapas
2. **arena_classica.yml** - Arena circular padrão (2-12 jogadores)
3. **arena_gigante.yml** - Arena grande (4-24 jogadores)

### Melhorias

- Sistema de configuração visual com `/game setpos`
- Coordenadas formatadas automaticamente para copiar
- Validação de mapas ao carregar
- Suporte a múltiplos spawns por mapa
- Altura de morte configurável por mapa

### Comandos Mantidos

- `/evento` - Entrar na sala de eventos
- `/game <evento> listmaps` - Listar mapas do evento
- `/game <evento> reload` - Recarregar evento
- `/game <evento> info` - Informações do evento

## Versão 1.0.0 - Lançamento Inicial

### Funcionalidades

- Sistema base de eventos
- Evento Sumo implementado
- Comando `/evento` para entrar
- Sistema de kills e deaths
- Botão "Jogar novamente"
- Fases de jogo (WAITING, STARTING, PLAYING, ENDING, RESETTING)
