output "n8n_instance_id" {
  description = "EC2 instance ID for n8n."
  value       = aws_instance.n8n.id
}

output "n8n_public_ip" {
  description = "Public IPv4 address of the n8n instance."
  value       = aws_instance.n8n.public_ip
}

output "n8n_url" {
  description = "HTTP URL for the n8n lab instance."
  value       = "http://${aws_instance.n8n.public_ip}:5678"
}
