export class Message {
  static fromJSON (buffer) {
    return new Message(JSON.parse(buffer.toString()))
  }

  constructor ({ username, command, contents, timestamp }) {
    this.username = username
    this.command = command
    this.contents = contents
    this.timestamp = timestamp
  }
// use to send
  toJSON () {
    return JSON.stringify({
      username: this.username,
      command: this.command,
      contents: this.contents,
      timestamp: this.timestamp
    })
  }
// use to display
  toString () {
    let st = `${this.timestamp}: `
    switch (this.command) {
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
    return st
  }
}
