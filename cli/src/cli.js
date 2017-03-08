import vorpal from 'vorpal'
import { words } from 'lodash'
import { connect } from 'net'
import { Message } from './Message'

export const cli = vorpal()

let username
let host = 'localhost'
let port = 8080
let server

cli
  .delimiter(cli.chalk['yellow']('ftd~$'))

cli
  .mode('connect <username> [host] [port]')
  .delimiter(cli.chalk['green']('connected>'))
  .init(function (args, callback) {
    username = args.username
    if (args.host) host = args.host
    if (args.port) port = args.port
    server = connect({ host, port }, () => {
      server.write(new Message({ username, command: 'connect' }).toJSON() + '\n')
      callback()
    })

    server.on('data', (buffer) => {
      const mess = Message.fromJSON(buffer)
      let color
      switch (mess.command) {
        case 'echo':
          color = 'white'
          break
        case 'broadcast':
          color = 'red'
          break
        case 'users':
          color = 'blue'
          break
        default:
          color = 'magenta'
          break
      }
      this.log(cli.chalk[color](mess.toString()))
    })

    server.on('end', () => {
      cli.exec('exit')
    })
  })
  .action(function (input, callback) {
    const [ command, ...rest ] = words(input, /[@\w]+/g)
    const contents = rest.join(' ')
    if (command === 'disconnect') {
      server.end(new Message({ username, command }).toJSON() + '\n')
    } else if (command === 'echo' || command === 'broadcast' || command === 'users') {
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
    } else if (command.match(/@*/)) {
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
    } else {
      this.log(`Command <${command}> was not recognized`)
    }

    callback()
  })
