# Angular proxy configuration snippet

Dans `angular.json`, vérifier que la cible `serve` référence le proxy :

```json
{
  "projects": {
    "my-angular-app": {
      "architect": {
        "serve": {
          "options": {
            "proxyConfig": "proxy.conf.json"
          }
        }
      }
    }
  }
}
```

Les services Angular doivent appeler l’API avec des chemins relatifs :

```ts
this.http.get('/api/users');
```

Ne pas utiliser :

```ts
this.http.get('http://localhost:8080/api/users');
```
