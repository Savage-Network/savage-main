# SavageMC HungerGames

Um sistema completo de HungerGames construído do zero para a rede SavageMC, com arquitetura moderna e total compatibilidade com os padrões atuais.

## 🚀 Características

### 🎮 Sistema de Jogo
- **Gerenciamento de múltiplas partidas**: Suporte para várias instâncias simultâneas
- **Estados centralizados**: WAITING → STARTING → INVINCIBILITY → PLAYING → FEAST → FINAL_BATTLE → ENDING → RESTARTING
- **Controle automático**: Início, feast, batalha final e reinício automáticos
- **Sistema de espectadores**: Espectação completa com modo espectador

### 🏟️ Sistema de Arenas
- **Multi-arena**: Suporte para múltiplas arenas
- **Carregamento dinâmico**: Carregamento e validação de arenas
- **Configuração flexível**: Spawns, feast, bordas e regras personalizáveis
- **Gerenciamento completo**: Habilitar/desabilitar arenas, seleção automática

### 🎯 Sistema de Kits
- **Arquitetura modular**: Sistema de plugins de kits extensível
- **Registro dinâmico**: Kits registrados em tempo de execução
- **Permissões integradas**: Controle de acesso baseado em ranks
- **Interface moderna**: Seleção e gerenciamento intuitivo

### 📊 Sistema de Usuários
- **Estatísticas completas**: Kills, mortes, vitórias, K/D ratio
- **Gerenciamento de estado**: Lobby, jogando, espectador, vencedor
- **Integração SavageMC**: Contas, ranks, permissões nativas
- **Persistência**: Salvamento automático de dados

### 🎉 Celebrações
- **Celebrações dinâmicas**: Padrão, VIP e especial
- **Efeitos visuais**: Partículas, fireworks, sons
- **Personalização por rank**: Celebrações diferentes para cada rank
- **Grand finale**: Efeitos espetaculares para vencedores

### ⚙️ Performance e Estabilidade
- **Scheduler otimizado**: Gerenciamento eficiente de tasks
- **Limpeza automática**: Remoção de entidades e otimização de memória
- **Monitoramento**: Estatísticas em tempo real e métricas
- **Compatibilidade**: Suporte para Folia e versões modernas

## 📋 Comandos

### Comandos Básicos
- `/help` - Mostra ajuda do HungerGames
- `/kit <nome>` - Seleciona um kit
- `/kits [detailed]` - Lista kits disponíveis
- `/stats [jogador]` - Mostra estatísticas
- `/spectate` - Entra no modo espectador

### Comandos de Jogo
- `/start` - Inicia uma partida
- `/stop` - Para a partida atual
- `/arena <subcmd>` - Gerencia arenas
- `/feast <subcmd>` - Gerencia o feast

### Comandos Administrativos
- `/forcekit <jogador> <kit>` - Força kit para jogador

## 🔧 Configuração

O arquivo `config.yml` contém todas as configurações principais:

```yaml
game:
  minimum-players: 2
  maximum-players: 24
  auto-start: true
  invincibility-duration: 60
  feast-time: 300

arena:
  default-arena: "default"
  random-selection: true
  validate-on-load: true

kits:
  enabled: true
  double-kit: false
  cooldown: 0
```

## 🏗️ Arquitetura

### Estrutura Principal
```
com.minecraft.savagemc.hungergames/
├── HungerGamesPlugin.java          # Classe principal
├── arena/                         # Sistema de arenas
│   ├── ArenaManager.java
│   └── Arena.java
├── game/                          # Sistema de jogos
│   ├── GameManager.java
│   ├── instance/
│   │   ├── GameInstance.java
│   │   └── GamePlayer.java
│   ├── state/
│   │   ├── GameState.java
│   │   └── GameStateManager.java
│   └── scheduler/
│       ├── SchedulerManager.java
│       ├── GameCheckTask.java
│       ├── CleanupTask.java
│       └── StatsUpdateTask.java
├── user/                          # Sistema de usuários
│   ├── UserManager.java
│   ├── User.java
│   ├── UserState.java
│   ├── KitManager.java
│   ├── kits/
│   │   ├── Kit.java
│   │   ├── KitRegistry.java
│   │   └── list/
│   └── celebrations/
│       ├── CelebrationManager.java
│       ├── Celebration.java
│       └── implementations/
├── command/                       # Sistema de comandos
│   ├── CommandManager.java
│   ├── HungerGamesCommand.java
│   └── implementations/
├── listener/                      # Sistema de eventos
│   ├── EventManager.java
│   ├── GameListener.java
│   ├── PlayerListener.java
│   ├── WorldListener.java
│   ├── CombatListener.java
│   └── ConnectionListener.java
└── config/
    └── ConfigurationManager.java
```

### Padrões de Projeto
- **Injeção de dependências**: Gerenciadores injetados via construtor
- **Event-driven**: Sistema baseado em eventos Bukkit
- **State machine**: Máquina de estados para gerenciamento de jogos
- **Plugin architecture**: Arquitetura de plugins para kits extensível
- **Clean architecture**: Separação clara de responsabilidades

## 🔌 Integrações

### SavageMC Core
- **Sistema de contas**: Integração completa com Account system
- **Ranks e permissões**: Uso nativo do sistema de ranks SavageMC
- **Database**: Conexão PostgreSQL via SavageCore
- **Redis**: Cache e comunicação via SavageCore Redis

### APIs Externas
- **PlaceholderAPI**: Suporte para placeholders
- **Vault**: Integração econômica (opcional)
- **WorldEdit**: Suporte para edição de arenas (opcional)

## 📦 Dependências

### Obrigatórias
- SavageCore (core-bukkit)
- Java 17+
- Paper/Spigot 1.20+

### Opcionais
- PlaceholderAPI
- Vault
- WorldEdit

## 🚀 Instalação

1. Coloque o plugin na pasta `plugins/`
2. Inicie o servidor para gerar configurações
3. Configure as arenas em `plugins/HungerGames/arenas/`
4. Ajuste as configurações em `config.yml`
5. Reinicie o servidor

## 📝 Desenvolvimento

### Adicionando Novos Kits

```java
public class ExemploKit extends Kit {
    
    public ExemploKit(HungerGamesPlugin plugin) {
        super(plugin);
        setDisplayName("Exemplo");
        setDescription("Um kit de exemplo");
        setIcon(Material.DIAMOND_SWORD);
        setItems(new ItemStack(Material.DIAMOND_SWORD));
    }
    
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        // Lógica do kit
    }
}
```

### Criando Novas Celebrações

```java
public class CustomCelebration extends Celebration {
    
    public CustomCelebration(HungerGamesPlugin plugin) {
        super(plugin, "custom");
    }
    
    @Override
    protected void onStart(Player player) {
        // Efeitos personalizados
    }
    
    @Override
    protected void onStop() {
        // Limpeza
    }
}
```

## 🐛 Troubleshooting

### Problemas Comuns

**Jogo não inicia:**
- Verifique se há jogadores suficientes
- Confirme se a arena está configurada
- Verifique permissões

**Kits não funcionam:**
- Verifique se os kits estão registrados
- Confirme as permissões do jogador
- Verifique se o kit está habilitado

**Performance baixa:**
- Ajuste os intervals de cleanup
- Verifique uso de memória
- Considere reduzir entidades

## 📄 Licença

Este projeto é propriedade da SavageMC. Todos os direitos reservados.

## 🤝 Contribuição

Contribuições são bem-vindas! Por favor:
1. Faça fork do projeto
2. Crie uma branch para sua feature
3. Faça commit das mudanças
4. Abra um pull request

## 📞 Suporte

Para suporte, contate a equipe SavageMC ou abra uma issue no repositório.
