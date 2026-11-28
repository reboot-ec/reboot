# n8n en EC2

El módulo ejecuta n8n y PostgreSQL en una instancia EC2. Los volúmenes de Docker guardan los datos en el disco gp3 cifrado de la instancia.

## Desplegar n8n

Desde `infrastructure/terraform/environments/qa` o `infrastructure/terraform/environments/prod`, completa las variables requeridas del entorno. Inyecta los secretos mediante `TF_VAR_n8n_db_password` y `TF_VAR_n8n_encryption_key` desde el almacén aprobado. Usa valores distintos por entorno y conserva la clave de cifrado para poder recuperar las credenciales de n8n. Limita el acceso al estado de Terraform y a los datos de arranque de EC2.

```powershell
terraform init
terraform plan -target="module.n8n"
terraform apply -target="module.n8n"
terraform output -raw n8n_url
terraform output -raw n8n_public_ip
```

Terraform puede crear o actualizar dependencias de red de n8n aunque el plan esté dirigido al módulo. Revisa el plan antes de aplicar y obtén aprobación antes de ejecutar `apply` en producción. [Selección de recursos en Terraform](https://developer.hashicorp.com/terraform/cli/commands/plan#resource-targeting)

## Crear el flujo de alerta

1. Abre `http://<n8n_public_ip>:5678` cuando termine el arranque y crea la cuenta propietaria. Usa el acceso HTTP para el laboratorio con datos de prueba. Para tráfico real, configura HTTPS en el editor y en el webhook.
2. Crea un flujo con un nodo **Webhook**. Configura **HTTP Method** como `POST`, **Path** como `login-alert` y **Respond** como **Immediately**. Configura **Header Auth** con una credencial cuyo encabezado y valor coincidan con los del emisor.
3. Conecta **Gmail** con la operación **Send** y una credencial OAuth2, o **Send Email** con una credencial SMTP. Para la alerta de laboratorio, fija una dirección de pruebas en **To**, usa el asunto `Alerta de inicio de sesión` y un mensaje breve en texto plano. Mantén las credenciales en n8n.
4. Guarda y activa el flujo. Copia la **Production URL** del nodo Webhook, que en el laboratorio será `http://<n8n_public_ip>:5678/webhook/login-alert`. [Nodo Webhook](https://docs.n8n.io/integrations/builtin/core-nodes/n8n-nodes-base.webhook/) · [Nodo Send Email](https://docs.n8n.io/integrations/builtin/core-nodes/n8n-nodes-base.sendemail/)

## Conectar identity-service

En el entorno donde se ejecuta `identity-service`, asigna la URL completa a la propiedad que consume su cliente de alertas de inicio de sesión. Configura también el encabezado de autenticación en ese cliente con la misma credencial del nodo Webhook. Reinicia o vuelve a desplegar el servicio para cargar la configuración. El nombre de la propiedad del cliente debe confirmarse en la configuración de Spring Boot de la versión desplegada.

El emisor debe enviar un `POST` tras autenticar al usuario correctamente. Usa un cuerpo mínimo, por ejemplo `{"event":"login.succeeded"}`, y omite contraseñas, tokens y datos personales innecesarios. Si la versión desplegada carece de cliente para esa alerta, hace falta implementar el envío en Identity antes de que el flujo pueda dispararse desde un inicio de sesión.

Comprueba el flujo iniciando sesión con una cuenta de prueba. Revisa que la ejecución aparezca en **Executions** y que llegue el correo a la dirección configurada. Si cambia la IP pública tras detener e iniciar EC2, actualiza `WEBHOOK_URL` en `/opt/n8n/.env`, recrea el contenedor y actualiza la URL en Identity.
